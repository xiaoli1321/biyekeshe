import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { parseStreamBuffer } from '@/utils/streamParser'
import { startWorkflowStream, abortWorkflow } from '@/services/api/workflow'
import type { WorkflowRunRequest } from '@/services/api/workflow'

// ============== 类型定义 ==============

/** 每个步骤的消息对象（在 UI 中实时更新） */
export interface StepMessage {
  id: string
  type: string
  content: string       // 正文 Markdown（逐 token 追加）
  thinkingProcess: string // 思维链内容
  isThinkingProcess: boolean
}

// ============== Pinia Store ==============

export const useWorkflowStore = defineStore('workflow', () => {

  // ---- 运行状态 ----
  const isRunning = ref(false)
  const isFinished = ref(false)
  const errorMsg = ref('')

  // ---- 步骤消息列表（与 JS 版 allMessageValue.message_lists 对应）----
  const stepMessages = ref<StepMessage[]>([])
  const currentStepIndex = ref(-1)   // 当前正在更新的步骤索引

  // ---- 已完成步骤 ID 列表（后端发来）----
  const finishedStepIds = ref<string[]>([])

  // ---- 最终汇总 Markdown 列表（用于生成 Word）----
  const showHistoryMdList = ref<any[]>([])
  const historyLists = ref<any[]>([])

  // ---- Word 文件 URL（目前 MVP 不调 WPS 服务，预留）----
  const wordUrl = ref('')

  // ---- 中断控制 ----
  let abortController: AbortController | null = null
  const activeStreamingId = ref('')

  // ---- 计算属性 ----
  const hasContent = computed(() => stepMessages.value.length > 0)
  const fullMarkdown = computed(() =>
    stepMessages.value
      .filter(m => !m.isThinkingProcess)
      .map(m => m.content)
      .join('\n\n')
  )

  // ============== 核心方法 ==============

  /**
   * 启动工作流流式执行
   * 等价于原 React 项目的 streaming_llm_socket_with_prompt_list
   */
  async function runWorkflow(request: WorkflowRunRequest) {
    // 重置状态
    isRunning.value = true
    isFinished.value = false
    errorMsg.value = ''
    stepMessages.value = []
    currentStepIndex.value = -1
    finishedStepIds.value = []
    showHistoryMdList.value = []
    historyLists.value = []
    wordUrl.value = ''
    activeStreamingId.value = request.streamingId

    abortController = new AbortController()

    try {
      const response = await startWorkflowStream(request, abortController.signal)
      const reader = response.body!.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break
        if (abortController.signal.aborted) break

        const chunk = decoder.decode(value, { stream: true })
        buffer += chunk

        const { events, remaining } = parseStreamBuffer(buffer)
        buffer = remaining

        for (const evt of events) {
          handleStreamEvent(evt.event, evt.content, evt.payload)
        }
      }

      reader.releaseLock()
    } catch (err: any) {
      if (err.name !== 'AbortError') {
        errorMsg.value = err.message || '工作流执行失败'
      }
    } finally {
      isRunning.value = false
      isFinished.value = true
    }
  }

  /**
   * 处理单个 SSE 事件
   */
  function handleStreamEvent(event: string, content: string, payload: any) {
    switch (event) {

      case 'AGENT_START_STEP_NOW': {
        // 新步骤开始，push 一个空消息槽
        const info = payload || {}
        const newMsg: StepMessage = {
          id: info.id || `step_${stepMessages.value.length}`,
          type: info.type || 'text',
          content: '',
          thinkingProcess: '',
          isThinkingProcess: false
        }
        stepMessages.value.push(newMsg)
        currentStepIndex.value = stepMessages.value.length - 1
        break
      }

      case 'token': {
        // 纯文本 token（LLM 逐字输出）
        const idx = currentStepIndex.value
        if (idx >= 0 && idx < stepMessages.value.length) {
          stepMessages.value[idx].content += content
        } else {
          // 还没收到 AGENT_START_STEP_NOW 就来了 token，自动创建
          stepMessages.value.push({
            id: `auto_${Date.now()}`,
            type: 'text',
            content,
            thinkingProcess: '',
            isThinkingProcess: false
          })
          currentStepIndex.value = stepMessages.value.length - 1
        }
        break
      }

      case 'WF_AGENT_STARTS':
        // 工作流开始，已在 runWorkflow 重置了状态
        break

      case 'WF_AGENT_ENDS':
        isFinished.value = true
        isRunning.value = false
        break

      case 'WF_AGENT_ABORTED':
        isFinished.value = true
        isRunning.value = false
        errorMsg.value = '工作流已被中断'
        break

      case 'AGENT_RECEIVE_FINISHED_FLOW_HISTORY_DATA': {
        // 全部完成，设置历史数据
        try {
          const data = payload || (content ? JSON.parse(content) : {})
          if (data.finished_step_id_list) finishedStepIds.value = data.finished_step_id_list
          if (data.show_history_md_list) showHistoryMdList.value = data.show_history_md_list
          if (data.history) historyLists.value = data.history
        } catch {}
        break
      }

      case 'WORD_FILE_URL':
        wordUrl.value = content
        break

      default:
        break
    }
  }

  /**
   * 中断工作流
   */
  async function stopWorkflow() {
    if (abortController) {
      abortController.abort()
      abortController = null
    }
    if (activeStreamingId.value) {
      try {
        await abortWorkflow(activeStreamingId.value)
      } catch {}
    }
    isRunning.value = false
  }

  /**
   * 重置状态
   */
  function reset() {
    isRunning.value = false
    isFinished.value = false
    errorMsg.value = ''
    stepMessages.value = []
    currentStepIndex.value = -1
    finishedStepIds.value = []
    showHistoryMdList.value = []
    historyLists.value = []
    wordUrl.value = ''
    activeStreamingId.value = ''
  }

  return {
    // 状态
    isRunning, isFinished, errorMsg,
    stepMessages, currentStepIndex,
    finishedStepIds, showHistoryMdList, historyLists,
    wordUrl, activeStreamingId,
    // 计算属性
    hasContent, fullMarkdown,
    // 方法
    runWorkflow, stopWorkflow, reset
  }
})

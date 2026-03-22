import http from '@/services/http'
// ============== 工作流步骤类型 ==============
export interface WorkflowStep {
  id: string
  type: 'text' | 'fixed' | 'table' | 'pic' | 'classifier' | 'rag' | string
  content: string
  title?: string
  model?: string
  systemPrompt?: string
  historyMode?: 'all' | 'none' | 'file_history_only' | 'processed_only'
  isThinkingProcess?: boolean
  picType?: 'mindMap' | 'flowchart' | 'sequenceDiagram'
}

export interface HistoryMessage {
  role: 'user' | 'assistant' | 'system'
  content: string
}

export interface WorkflowRunRequest {
  rawMsgList: WorkflowStep[]
  chatHistoryList?: HistoryMessage[]
  streamingId: string
  generateFile?: boolean
  workflowName?: string
  returnMode?: 'api'
  predefinedVariables?: Record<string, string>
}

// ============== API 函数 ==============

/**
 * 启动工作流（流式，返回 Response 对象供 ReadableStream 消费）
 * 因为需要读取流，不走 axios，直接用 fetch
 */
export async function startWorkflowStream(
  request: WorkflowRunRequest,
  signal?: AbortSignal
): Promise<Response> {
  const token = localStorage.getItem('token')
  const response = await fetch('/api/llm/streaming_workflow', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(request),
    signal
  })

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}: ${response.statusText}`)
  }
  return response
}

/**
 * 中断工作流
 */
export async function abortWorkflow(streamingId: string): Promise<void> {
  await http.post('/llm/abort_streaming', { streamingId })
}

/**
 * 获取内置工作流模板列表（本项目 MVP 阶段返回硬编码模板）
 */
export interface WorkflowTemplate {
  id: string
  name: string
  description: string
  steps: WorkflowStep[]
}

export function getBuiltinTemplates(): WorkflowTemplate[] {
  return [
    {
      id: 'report_basic',
      name: '通用分析报告',
      description: '根据输入内容，自动生成结构化分析报告（摘要 + 详细分析 + 结论）',
      steps: [
        { id: 's0', type: 'fixed', content: '# 分析报告\n\n' },
        {
          id: 's1',
          type: 'text',
          title: '## 一、执行摘要\n',
          content: '请根据以下主题，生成一段200字以内的执行摘要：[TOPIC]',
          model: 'deepseek_v3',
          historyMode: 'none'
        },
        {
          id: 's2',
          type: 'text',
          title: '\n## 二、详细分析\n',
          content: '请针对以下主题进行深入分析，包含背景、现状、趋势三个维度：[TOPIC]',
          model: 'deepseek_v3',
          historyMode: 'none'
        },
        {
          id: 's3',
          type: 'text',
          title: '\n## 三、结论与建议\n',
          content: '基于前面的分析，请给出3-5条可行的结论和建议。',
          model: 'deepseek_v3',
          historyMode: 'all'
        }
      ]
    },
    {
      id: 'course_design',
      name: '课程设计方案',
      description: '自动生成一份完整的课程设计方案，包含目标、大纲和教学方法',
      steps: [
        { id: 'c0', type: 'fixed', content: '# 课程设计方案\n\n' },
        {
          id: 'c1',
          type: 'text',
          title: '## 课程目标\n',
          content: '为"[TOPIC]"课程制定3-5个明确的学习目标，使用可量化的行为动词描述。',
          model: 'deepseek_v3',
          historyMode: 'none'
        },
        {
          id: 'c2',
          type: 'table',
          title: '\n## 课程大纲\n',
          content: '请为"[TOPIC]"课程生成一份详细的教学大纲，包含章节名称、课时和核心内容，用表格展示。',
          model: 'deepseek_v3',
          historyMode: 'none'
        },
        {
          id: 'c3',
          type: 'text',
          title: '\n## 教学方法\n',
          content: '基于上述课程目标和大纲，推荐适合的教学方法和评估方式。',
          model: 'deepseek_v3',
          historyMode: 'all'
        }
      ]
    },
    {
      id: 'custom',
      name: '自定义工作流',
      description: '从空白开始，自己添加步骤',
      steps: []
    }
  ]
}

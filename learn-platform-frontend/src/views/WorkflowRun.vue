<template>
  <div class="workflow-edit-page">
    <!-- 1. 顶部面包屑导航 -->
    <div class="workflow-breadcrumb border-bottom px-4 py-2">
      <nav class="d-flex align-items-center gap-2 small">
        <span class="text-muted hover-link cursor-pointer">AI 工作流</span>
        <i class="bi bi-chevron-right text-muted x-small"></i>
        <span class="text-white fw-bold">创建 & 运行工作流</span>
      </nav>
    </div>

    <div class="workflow-page-container">
      <!-- 2. 工具栏 (新增步按钮 & 运行控制) -->
      <div class="workflow-toolbar border-bottom px-4 py-3 d-flex align-items-center justify-content-between">
        <div class="d-flex gap-3">
          <!-- 模板快捷选择 -->
          <div class="input-group input-group-sm" style="width: 200px;">
            <label class="input-group-text bg-transparent border-end-0"><i class="bi bi-template"></i></label>
            <select v-model="selectedTemplateId" class="form-select bg-transparent" :disabled="isRunning">
              <option v-for="tpl in templates" :key="tpl.id" :value="tpl.id">{{ tpl.name }}</option>
            </select>
          </div>
          <button @click="addCustomStep" class="btn btn-sm btn-outline-primary" :disabled="isRunning">
            <i class="bi bi-plus-lg me-1"></i> 添加步骤
          </button>
        </div>

        <div class="d-flex gap-2">
          <div v-if="isRunning" class="status-indicator me-3 d-flex align-items-center gap-2">
            <div class="run-spinner"></div>
            <span class="text-primary small">正在执行第 {{ currentStepIndex + 1 }} 步...</span>
          </div>
          <button
            v-if="!isRunning"
            class="btn btn-primary btn-sm px-4"
            :disabled="!canRun"
            @click="handleRun"
          >
            <i class="bi bi-play-fill me-1"></i> {{ isFinished && hasContent ? '重新运行' : '立即执行' }}
          </button>
          <button v-else class="btn btn-danger btn-sm px-4" @click="handleStop">
            <i class="bi bi-stop-fill me-1"></i> 停止
          </button>
        </div>
      </div>

      <!-- 3. 主画布区 (横向滚动卡片) -->
      <div class="workflow-canvas p-4">
        <!-- 主题配置卡片 (必填) -->
        <div class="d-flex align-items-start gap-3 flex-nowrap pb-4 overflow-auto canvas-scroller">
          
          <!-- 初始化配置卡片 -->
          <div class="builder-card config-card">
            <div class="card-header-simple">
              <i class="bi bi-gear-fill me-2 text-warning"></i> 基础配置
            </div>
            <div class="card-body-simple p-3">
              <div class="mb-3">
                <label class="small text-muted mb-1 d-block">主题关键词</label>
                <input v-model="topic" type="text" class="form-control form-control-sm" placeholder="AI 教育影响..." />
              </div>
              <div>
                <label class="small text-muted mb-1 d-block">报告标题</label>
                <input v-model="workflowName" type="text" class="form-control form-control-sm" placeholder="可选标题..." />
              </div>
            </div>
          </div>

          <!-- 步骤连接线组件 (逻辑箭头) -->
          <div class="step-connector mt-5"><i class="bi bi-arrow-right"></i></div>

          <!-- 动态步骤卡片流 -->
          <template v-for="(step, idx) in currentSteps" :key="step.id">
            <div class="builder-card" :class="{ 'card-running': idx === currentStepIndex && isRunning }">
              <div class="card-header-simple d-flex justify-content-between">
                <div>
                  <span class="text-muted small me-2">#{{ idx + 1 }}</span>
                  <select v-model="step.type" class="type-select-inline">
                    <option value="text">文字生成</option>
                    <option value="fixed">固定内容</option>
                    <option value="table">表格数据</option>
                  </select>
                </div>
                <div class="actions" v-if="selectedTemplateId === 'custom' && !isRunning">
                  <i class="bi bi-trash text-danger cursor-pointer x-small" @click="removeCustomStep(idx)"></i>
                </div>
              </div>
              <div class="card-body-simple p-0">
                <input v-model="step.title" class="title-input-inline" placeholder="步骤标题..." />
                <textarea 
                  v-model="step.content" 
                  class="prompt-textarea-inline" 
                  placeholder="输入提示词 Prompt..."
                  :rows="selectedTemplateId === 'custom' ? 4 : 2"
                ></textarea>
                <div class="card-footer-simple d-flex justify-content-between align-items-center">
                   <div class="form-check form-check-sm mb-0">
                     <input v-model="step.isThinkingProcess" type="checkbox" class="form-check-input" :id="'th-'+idx">
                     <label class="form-check-label x-small text-muted" :for="'th-'+idx">思维链</label>
                   </div>
                   <div v-if="idx < currentStepIndex || (isFinished && !isRunning)" class="text-success x-small fw-bold">
                     <i class="bi bi-check-lg"></i> 已完成
                   </div>
                </div>
              </div>
            </div>
            <!-- 连接箭头 -->
            <div v-if="idx < currentSteps.length - 1" class="step-connector mt-5"><i class="bi bi-arrow-right"></i></div>
          </template>
        </div>
      </div>

      <!-- 4. 结果查看区 (向下滚动) -->
      <div class="workflow-results-area border-top bg-black-dark p-4 mt-2">
        <div class="results-header mb-4 d-flex justify-content-between align-items-center">
            <h5 class="mb-0 fw-bold"><i class="bi bi-terminal me-2"></i> 生成结果预览</h5>
            <div class="d-flex gap-2">
              <button v-if="isFinished && hasContent" @click="exportMarkdown" class="btn btn-sm btn-outline-secondary">
                <i class="bi bi-download"></i> 导出 Markdown
              </button>
            </div>
        </div>
        
        <!-- 空状态保持 -->
        <div v-if="!hasContent && !isRunning" class="empty-placeholder py-5 text-center text-muted">
           <i class="bi bi-robot display-4 mb-3 d-block"></i>
           <p>请在上方编排工作流，点击「立即执行」观察实时产出</p>
        </div>

        <!-- 结果列表 -->
        <div class="results-scroller" ref="stepsResultRef">
           <StepCard
             v-for="(msg, idx) in stepMessages"
             :key="msg.id"
             :message="msg"
             :is-current-step="idx === currentStepIndex && isRunning"
             :is-running="isRunning"
           />
           <!-- 运行时占位 -->
           <div v-if="isRunning" class="py-3 px-4 border-dashed rounded text-muted small d-flex align-items-center gap-2">
              <span class="spinner-grow spinner-grow-sm text-primary"></span> 智能体思考中...
           </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { storeToRefs } from 'pinia'

const uuidv4 = () => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

import StepCard from '@/components/workflow/StepCard.vue'
import { useWorkflowStore } from '@/stores/workflow'
import { getBuiltinTemplates } from '@/services/api/workflow'
import type { WorkflowTemplate } from '@/services/api/workflow'

const workflowStore = useWorkflowStore()
const {
  isRunning,
  isFinished,
  stepMessages,
  currentStepIndex,
  hasContent
} = storeToRefs(workflowStore)

// ---- 模板选择 ----
const templates = ref<WorkflowTemplate[]>(getBuiltinTemplates())
const selectedTemplateId = ref('report_basic')
const selectedTemplate = computed(() =>
  templates.value.find(t => t.id === selectedTemplateId.value) || templates.value[0]
)

// 自定义步骤存储
const customSteps = ref<any[]>([
  { id: uuidv4(), type: 'text', title: '自定义步骤 1', content: '请针对 [TOPIC] 进行分析', isThinkingProcess: false }
])

const currentSteps = computed(() => {
  if (selectedTemplateId.value === 'custom') {
    return customSteps.value
  }
  return selectedTemplate.value?.steps || []
})

const addCustomStep = () => {
  customSteps.value.push({
    id: uuidv4(),
    type: 'text',
    title: `自定义步骤 ${customSteps.value.length + 1}`,
    content: '',
    isThinkingProcess: false
  })
}

const removeCustomStep = (idx: number) => {
  customSteps.value.splice(idx, 1)
}

// ---- 表单变量 ----
const topic = ref('')
const workflowName = ref('')

// ---- 是否可以运行 ----
const canRun = computed(() =>
  currentSteps.value.length > 0 && topic.value.trim().length > 0
)

// ---- 步骤结果容器 ref（用于滚动）----
const stepsResultRef = ref<HTMLElement | null>(null)

// ---- 运行 ----
const handleRun = async () => {
  workflowStore.reset()

  const id = uuidv4()
  const topicVar = topic.value.trim()

  // 将 [TOPIC] 替换到步骤 content/title
  const processedSteps = currentSteps.value.map(s => ({
    ...s,
    content: s.content?.replace(/\[TOPIC\]/g, topicVar) || s.content,
    title: s.title?.replace(/\[TOPIC\]/g, topicVar) || s.title,
  }))

  await workflowStore.runWorkflow({
    rawMsgList: processedSteps,
    streamingId: id,
    generateFile: false,
    workflowName: workflowName.value || topicVar,
    returnMode: 'api'
  })
}

// ---- 停止 ----
const handleStop = () => {
  workflowStore.stopWorkflow()
}

// ---- 导出 Markdown ----
const exportMarkdown = () => {
  const md = workflowStore.fullMarkdown
  const blob = new Blob([md], { type: 'text/markdown;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `${workflowName.value || '工作流报告'}.md`
  a.click()
  URL.revokeObjectURL(a.href)
}

// ---- 自动滚动到最新内容 ----
watch(
  () => workflowStore.stepMessages.length,
  async () => {
    await nextTick()
    if (stepsResultRef.value) {
      stepsResultRef.value.scrollTop = stepsResultRef.value.scrollHeight
    }
    window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' })
  }
)
</script>

<style scoped>
.workflow-page-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 110px);
  background-color: #050505;
  color: #ededed;
}

/* 面包屑 */
.workflow-breadcrumb {
  background-color: var(--bg-color);
  border-color: var(--border-color) !important;
}
.x-small { font-size: 0.7rem; }
.hover-link:hover { color: var(--primary-color) !important; transition: color 0.2s; }

/* 工具栏 */
.workflow-toolbar {
  background-color: #f8fafc;
  border-color: var(--border-color) !important;
}

/* 画布主区 */
.workflow-canvas {
  flex: 1;
  min-height: 420px;
  background-color: #ffffff;
  background-image: radial-gradient(#e5e7eb 1px, transparent 1px);
  background-size: 32px 32px;
  overflow: hidden;
  display: flex;
  align-items: center;
}

.canvas-scroller {
  width: 100%;
  padding: 30px;
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 transparent;
}

/* 步骤卡片 */
.builder-card {
  width: 320px;
  flex-shrink: 0;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.builder-card:hover {
  border-color: #cbd5e1;
  transform: translateY(-4px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

.card-running {
  border-color: var(--primary-color) !important;
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.1);
  animation: border-pulse 2s infinite;
}

@keyframes border-pulse {
  0%, 100% { border-color: var(--primary-color); }
  50% { border-color: #818cf8; }
}

.card-header-simple {
  padding: 12px 16px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  font-size: 0.85rem;
  font-weight: 600;
  color: #64748b;
}

.type-select-inline {
  background: #fff;
  border: 1px solid #e2e8f0;
  color: var(--primary-color);
  font-size: 0.75rem;
  border-radius: 4px;
  padding: 1px 4px;
  outline: none;
}

.title-input-inline {
  width: 100%;
  padding: 12px 16px;
  background: transparent;
  border: none;
  border-bottom: 1px solid #f1f5f9;
  color: #1e293b;
  font-size: 0.95rem;
  font-weight: 600;
  outline: none;
}

.prompt-textarea-inline {
  width: 100%;
  padding: 12px 16px;
  background: #ffffff;
  border: none;
  color: #475569;
  font-size: 0.85rem;
  line-height: 1.5;
  resize: none;
  outline: none;
}

.card-footer-simple {
  padding: 8px 16px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
}

/* 连接器 */
.step-connector {
  color: #cbd5e1;
  font-size: 1.2rem;
  padding: 0 10px;
}

/* 结果区 */
.workflow-results-area {
  height: 50%;
  overflow-y: auto;
  border-color: var(--border-color) !important;
  background-color: #f8fafc;
}

.bg-black-dark {
  background-color: #ffffff;
}

.results-scroller {
  max-width: 900px;
  margin: 0 auto;
}

/* 运行状态动画 */
.run-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(79, 70, 229, 0.1);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.border-dashed {
  border: 1px dashed var(--border-color);
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
</style>

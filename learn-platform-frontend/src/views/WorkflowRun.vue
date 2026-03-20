<template>
  <div class="main-wrapper">
    <NavBar />
    <div class="workflow-page content-wrapper">
      <div class="container-xl">

        <!-- 页面头部 -->
        <div class="page-header mb-4">
          <div class="page-title-area">
            <h1 class="page-title">
              <i class="bi bi-lightning-charge-fill me-2 text-primary"></i>
              AI 文档工作流
            </h1>
            <p class="page-subtitle text-muted">
              选择工作流模板，配置主题，一键生成结构化文档
            </p>
          </div>
        </div>

        <div class="workflow-layout">
          <!-- 左侧：配置面板 -->
          <div class="config-panel card">
            <div class="card-body">

              <!-- 步骤 1：选择模板 -->
              <div class="config-section">
                <h6 class="section-title">
                  <span class="step-num">1</span> 选择工作流模板
                </h6>
                <div class="template-grid">
                  <div
                    v-for="tpl in templates"
                    :key="tpl.id"
                    class="template-card"
                    :class="{ active: selectedTemplateId === tpl.id }"
                    @click="selectTemplate(tpl.id)"
                  >
                    <div class="tpl-icon">{{ tplIcon(tpl.id) }}</div>
                    <div class="tpl-info">
                      <div class="tpl-name">{{ tpl.name }}</div>
                      <div class="tpl-desc">{{ tpl.description }}</div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 步骤 2：填写主题/变量 -->
              <div class="config-section">
                <h6 class="section-title">
                  <span class="step-num">2</span> 填写内容主题
                </h6>
                <div class="mb-3">
                  <label class="form-label">主题关键词 <span class="text-danger">*</span></label>
                  <input
                    v-model="topic"
                    type="text"
                    class="form-control"
                    placeholder="例如：人工智能对教育的影响"
                    :disabled="isRunning"
                  />
                  <div class="form-text text-muted">
                    会自动替换步骤 prompt 中的 [TOPIC] 占位符
                  </div>
                </div>
                <div class="mb-3">
                  <label class="form-label">报告名称（可选）</label>
                  <input
                    v-model="workflowName"
                    type="text"
                    class="form-control"
                    placeholder="例如：AI教育影响分析报告"
                    :disabled="isRunning"
                  />
                </div>
              </div>

              <!-- 步骤 3：步骤预览 -->
              <div class="config-section">
                <h6 class="section-title">
                  <span class="step-num">3</span> 工作流步骤预览
                  <span class="badge bg-secondary ms-2">{{ currentSteps.length }} 步</span>
                </h6>
                <div class="steps-preview">
                  <div
                    v-for="(step, idx) in currentSteps"
                    :key="step.id"
                    class="step-item"
                  >
                    <div class="step-dot" :class="`dot-${step.type}`"></div>
                    <div class="step-label">
                      <span class="step-idx">{{ idx + 1 }}</span>
                      <span class="step-type-badge" :class="`type-${step.type}`">{{ step.type }}</span>
                      <span class="step-content-preview">
                        {{ (step.title || step.content?.slice(0, 40) || '').replace(/\[TOPIC\]/g, '[TOPIC]') }}
                      </span>
                    </div>
                  </div>
                  <div v-if="currentSteps.length === 0" class="text-muted small">
                    请先选择模板
                  </div>
                </div>
              </div>

              <!-- 运行按钮 -->
              <div class="run-area">
                <button
                  v-if="!isRunning"
                  class="btn btn-primary btn-run"
                  :disabled="!canRun"
                  @click="handleRun"
                >
                  <i class="bi bi-play-fill me-2"></i>
                  {{ isFinished && hasContent ? '重新运行' : '开始生成' }}
                </button>
                <button
                  v-else
                  class="btn btn-danger btn-run"
                  @click="handleStop"
                >
                  <i class="bi bi-stop-fill me-2"></i>
                  停止生成
                </button>

                <!-- 导出 Markdown -->
                <button
                  v-if="isFinished && hasContent"
                  class="btn btn-outline-secondary btn-export mt-2"
                  @click="exportMarkdown"
                >
                  <i class="bi bi-download me-1"></i>
                  导出 Markdown
                </button>
              </div>

              <!-- 错误提示 -->
              <div v-if="errorMsg" class="alert alert-danger mt-3 py-2 small" role="alert">
                <i class="bi bi-exclamation-triangle me-1"></i>
                {{ errorMsg }}
              </div>

            </div>
          </div>

          <!-- 右侧：实时结果面板 -->
          <div class="result-panel">

            <!-- 空态 -->
            <div v-if="!hasContent && !isRunning" class="empty-state card">
              <div class="empty-icon">🤖</div>
              <h5>等待生成</h5>
              <p class="text-muted">配置工作流后点击「开始生成」，AI 将实时输出文档内容</p>
              <div class="feature-list">
                <div class="feature-item"><i class="bi bi-check-circle-fill text-success me-2"></i>实时流式输出，打字机效果</div>
                <div class="feature-item"><i class="bi bi-check-circle-fill text-success me-2"></i>支持 Markdown 富文本渲染</div>
                <div class="feature-item"><i class="bi bi-check-circle-fill text-success me-2"></i>多步骤上下文关联生成</div>
                <div class="feature-item"><i class="bi bi-check-circle-fill text-success me-2"></i>一键导出 Markdown 文档</div>
              </div>
            </div>

            <!-- 运行中 / 有内容 -->
            <template v-else>
              <!-- 顶部状态栏 -->
              <div class="result-statusbar card mb-3">
                <div class="d-flex align-items-center gap-3 p-3">
                  <div v-if="isRunning" class="status-running">
                    <span class="run-dot"></span>
                    <span>正在生成... 第 {{ currentStepIndex + 1 }} 步</span>
                  </div>
                  <div v-else-if="isFinished" class="status-done">
                    <i class="bi bi-check-circle-fill text-success me-1"></i>
                    <span>生成完成，共 {{ stepMessages.length }} 个步骤</span>
                  </div>
                  <div class="ms-auto d-flex gap-2">
                    <button
                      v-if="isFinished"
                      class="btn btn-sm btn-outline-primary"
                      @click="scrollToTop"
                    >
                      <i class="bi bi-arrow-up"></i>
                    </button>
                  </div>
                </div>
              </div>

              <!-- 步骤卡片列表 -->
              <div class="steps-result" ref="stepsResultRef">
                <StepCard
                  v-for="(msg, idx) in stepMessages"
                  :key="msg.id"
                  :message="msg"
                  :is-current-step="idx === currentStepIndex && isRunning"
                  :is-running="isRunning"
                />

                <!-- 运行时底部 loading 占位 -->
                <div v-if="isRunning" class="step-loading card">
                  <div class="d-flex align-items-center gap-2 p-3">
                    <div class="spinner-border spinner-border-sm text-primary" role="status"></div>
                    <span class="text-muted small">AI 正在思考...</span>
                  </div>
                </div>
              </div>
            </template>

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

import NavBar from '@/components/NavBar.vue'
import StepCard from '@/components/workflow/StepCard.vue'
import { useWorkflowStore } from '@/stores/workflow'
import { getBuiltinTemplates } from '@/services/api/workflow'
import type { WorkflowTemplate } from '@/services/api/workflow'

const workflowStore = useWorkflowStore()
const {
  isRunning,
  isFinished,
  errorMsg,
  stepMessages,
  currentStepIndex,
  hasContent,
  wordUrl
} = storeToRefs(workflowStore)

// ---- 模板选择 ----
const templates = ref<WorkflowTemplate[]>(getBuiltinTemplates())
const selectedTemplateId = ref('report_basic')
const selectedTemplate = computed(() =>
  templates.value.find(t => t.id === selectedTemplateId.value) || templates.value[0]
)
const currentSteps = computed(() => selectedTemplate.value?.steps || [])

const selectTemplate = (id: string) => {
  selectedTemplateId.value = id
}

const tplIcon = (id: string) => {
  const map: Record<string, string> = {
    report_basic: '📄',
    course_design: '🎓',
    custom: '⚙️'
  }
  return map[id] || '📋'
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

// ---- 滚动到顶部 ----
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
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
.workflow-page {
  padding-top: 32px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-size: 1.8rem;
  font-weight: 700;
  margin: 0 0 6px;
  background: linear-gradient(135deg, #7aa2ff, #a5b4fc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: 0.95rem;
  margin: 0;
}

/* 布局 */
.workflow-layout {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 24px;
  align-items: start;
}

@media (max-width: 900px) {
  .workflow-layout {
    grid-template-columns: 1fr;
  }
}

/* 配置面板 */
.config-panel {
  position: sticky;
  top: 80px;
}

.config-section {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
}

.config-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--muted-color);
  text-transform: uppercase;
  letter-spacing: 0.6px;
  margin-bottom: 14px;
}

.step-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(122, 162, 255, 0.2);
  color: #7aa2ff;
  font-size: 0.7rem;
  font-weight: 800;
}

/* 模板选择 */
.template-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.template-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  cursor: pointer;
  transition: all 0.2s;
}

.template-card:hover {
  border-color: rgba(122, 162, 255, 0.35);
  background: rgba(122, 162, 255, 0.06);
}

.template-card.active {
  border-color: rgba(122, 162, 255, 0.6);
  background: rgba(122, 162, 255, 0.1);
}

.tpl-icon { font-size: 1.5rem; line-height: 1; }

.tpl-name { font-size: 0.88rem; font-weight: 600; color: var(--text-color); margin-bottom: 2px; }
.tpl-desc { font-size: 0.75rem; color: var(--muted-color); line-height: 1.4; }

/* 步骤预览 */
.steps-preview {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: 200px;
  overflow-y: auto;
  padding-right: 4px;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.8rem;
}

.step-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.dot-text    { background: #7aa2ff; }
.dot-fixed   { background: #22c55e; }
.dot-table   { background: #f59e0b; }
.dot-pic     { background: #a78bfa; }

.step-label { display: flex; align-items: center; gap: 6px; overflow: hidden; }

.step-idx {
  color: rgba(154, 163, 178, 0.6);
  flex-shrink: 0;
  width: 14px;
}

.step-type-badge {
  font-size: 0.68rem;
  padding: 1px 6px;
  border-radius: 4px;
  flex-shrink: 0;
}
.type-text  { background: rgba(122, 162, 255, 0.2); color: #7aa2ff; }
.type-fixed { background: rgba(34, 197, 94, 0.15);  color: #22c55e; }
.type-table { background: rgba(245, 158, 11, 0.15); color: #f59e0b; }
.type-pic   { background: rgba(167, 139, 250, 0.2); color: #a78bfa; }

.step-content-preview {
  color: var(--muted-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 运行按钮 */
.run-area {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-top: 4px;
}

.btn-run {
  width: 100%;
  padding: 12px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 10px;
  transition: all 0.25s;
}

.btn-run:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 30px rgba(76, 125, 255, 0.45);
}

.btn-export {
  width: 100%;
  font-size: 0.85rem;
}

/* 结果面板 */
.result-panel {
  min-height: 400px;
}

/* 空态 */
.empty-state {
  text-align: center;
  padding: 60px 40px;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 16px;
  filter: drop-shadow(0 0 20px rgba(122, 162, 255, 0.4));
}

.feature-list {
  text-align: left;
  display: inline-flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 20px;
  font-size: 0.88rem;
}

.feature-item { color: var(--muted-color); }

/* 状态栏 */
.result-statusbar {
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.status-running {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #7aa2ff;
  font-size: 0.9rem;
}

.run-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #7aa2ff;
  box-shadow: 0 0 8px #7aa2ff;
  animation: pulse 1.2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50%       { opacity: 0.5; transform: scale(0.85); }
}

.status-done {
  font-size: 0.9rem;
  color: #22c55e;
}

/* 步骤加载占位 */
.step-loading {
  border: 1px dashed rgba(122, 162, 255, 0.3);
  border-radius: 12px;
  margin-bottom: 12px;
}
</style>

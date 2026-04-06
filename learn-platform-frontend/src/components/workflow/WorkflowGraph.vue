<template>
  <div class="wf-preview-panel">
    <!-- Header -->
    <div class="wf-preview-header">
      <div class="wf-header-left">
        <div class="wf-icon-badge">
          <i class="bi bi-diagram-3-fill"></i>
        </div>
        <div>
          <h6 class="wf-title">{{ data.workflow_name }}</h6>
          <p class="wf-desc">{{ data.description }}</p>
        </div>
      </div>
      <div class="wf-header-right">
        <span class="wf-step-count">{{ data.steps.length }} 个步骤</span>
      </div>
    </div>

    <!-- Horizontal Step Chain -->
    <div class="wf-steps-scroll">
      <div class="wf-steps-track">
        <template v-for="(step, idx) in data.steps" :key="step.id">
          <div class="wf-step-card" :class="'wf-type-' + step.type">
            <div class="wf-step-top">
              <span class="wf-step-badge">#{{ idx + 1 }}</span>
              <span class="wf-type-label">
                <i :class="getTypeIcon(step.type)"></i>
                {{ getTypeLabel(step.type) }}
              </span>
            </div>
            <div class="wf-step-title">{{ cleanTitle(step.title) }}</div>
            <div class="wf-step-prompt">{{ step.content }}</div>
            <div class="wf-step-meta">
              <span class="wf-meta-tag" v-if="step.historyMode === 'all'">
                <i class="bi bi-clock-history"></i> 全历史
              </span>
              <span class="wf-meta-tag" v-else>
                <i class="bi bi-dash-circle"></i> 独立
              </span>
            </div>
          </div>
          <div v-if="idx < data.steps.length - 1" class="wf-connector">
            <i class="bi bi-arrow-right"></i>
          </div>
        </template>
      </div>
    </div>

    <!-- Execute Button -->
    <div class="wf-execute-bar">
      <button class="wf-execute-btn" @click="handleExecute">
        <i class="bi bi-play-circle-fill me-2"></i>
        一键执行此工作流
      </button>
      <span class="wf-execute-hint">将跳转至工作流执行页面，由 AI 逐步生成内容</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useWorkflowStore } from '@/stores/workflow'

interface WorkflowStep {
  id: string
  type: string
  title?: string
  content: string
  historyMode?: string
  isThinkingProcess?: boolean
}

interface WorkflowData {
  workflow_name: string
  description: string
  steps: WorkflowStep[]
}

const props = defineProps<{
  data: WorkflowData
}>()

const router = useRouter()
const workflowStore = useWorkflowStore()

const cleanTitle = (title?: string) => {
  if (!title) return '未命名步骤'
  return title.replace(/^#+\s*/, '').trim()
}

const getTypeIcon = (type: string) => {
  const map: Record<string, string> = {
    text: 'bi bi-chat-left-text-fill',
    fixed: 'bi bi-pin-angle-fill',
    table: 'bi bi-table'
  }
  return map[type] || 'bi bi-puzzle-fill'
}

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    text: 'AI 生成',
    fixed: '固定内容',
    table: '表格生成'
  }
  return map[type] || type
}

const handleExecute = () => {
  // Store steps for WorkflowRun to pick up
  sessionStorage.setItem('ai_workflow_steps', JSON.stringify(props.data.steps))
  sessionStorage.setItem('ai_workflow_name', props.data.workflow_name)
  router.push('/workflow?from=ai')
}
</script>

<style scoped>
.wf-preview-panel {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  border: 1px solid rgba(99, 102, 241, 0.3);
  border-radius: 16px;
  padding: 0;
  margin-top: 12px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(99, 102, 241, 0.1) inset;
  width: 100%;
  max-width: none;
}

/* Header */
.wf-preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.02);
}

.wf-header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.wf-icon-badge {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.1rem;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
}

.wf-title {
  color: #e2e8f0;
  font-weight: 700;
  font-size: 1rem;
  margin: 0;
}

.wf-desc {
  color: #94a3b8;
  font-size: 0.78rem;
  margin: 2px 0 0 0;
}

.wf-step-count {
  background: rgba(99, 102, 241, 0.15);
  color: #818cf8;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  border: 1px solid rgba(99, 102, 241, 0.3);
}

/* Step Chain */
.wf-steps-scroll {
  overflow-x: auto;
  padding: 24px;
  scrollbar-width: thin;
  scrollbar-color: #334155 transparent;
}

.wf-steps-scroll::-webkit-scrollbar { height: 6px; }
.wf-steps-scroll::-webkit-scrollbar-thumb { background: #334155; border-radius: 3px; }
.wf-steps-scroll::-webkit-scrollbar-track { background: transparent; }

.wf-steps-track {
  display: flex;
  align-items: stretch;
  gap: 0;
  min-width: max-content;
}

/* Step Card */
.wf-step-card {
  width: 240px;
  min-height: 160px;
  background: rgba(30, 41, 59, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
  transition: all 0.3s ease;
  position: relative;
}

.wf-step-card:hover {
  transform: translateY(-3px);
  border-color: rgba(99, 102, 241, 0.5);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

/* Type-specific glow */
.wf-step-card.wf-type-text { border-left: 3px solid #6366f1; }
.wf-step-card.wf-type-fixed { border-left: 3px solid #10b981; }
.wf-step-card.wf-type-table { border-left: 3px solid #f59e0b; }

.wf-step-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.wf-step-badge {
  background: rgba(255, 255, 255, 0.08);
  color: #94a3b8;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 6px;
  font-family: monospace;
}

.wf-type-label {
  font-size: 0.7rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.wf-type-text .wf-type-label { color: #818cf8; }
.wf-type-fixed .wf-type-label { color: #34d399; }
.wf-type-table .wf-type-label { color: #fbbf24; }

.wf-step-title {
  color: #e2e8f0;
  font-weight: 600;
  font-size: 0.85rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.wf-step-prompt {
  color: #64748b;
  font-size: 0.72rem;
  line-height: 1.5;
  flex: 1;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.wf-step-meta {
  display: flex;
  gap: 6px;
  margin-top: auto;
}

.wf-meta-tag {
  font-size: 0.65rem;
  color: #64748b;
  background: rgba(255, 255, 255, 0.04);
  padding: 2px 8px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* Connector */
.wf-connector {
  display: flex;
  align-items: center;
  padding: 0 8px;
  color: #475569;
  font-size: 1.2rem;
  flex-shrink: 0;
}

/* Execute Bar */
.wf-execute-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(99, 102, 241, 0.04);
}

.wf-execute-btn {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.4);
}

.wf-execute-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.6);
  filter: brightness(1.1);
}

.wf-execute-hint {
  color: #64748b;
  font-size: 0.75rem;
}
</style>

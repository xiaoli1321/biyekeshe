<template>
  <div class="wf-step-card" :class="{ 'thinking': message.isThinkingProcess }">
    <div class="step-header">
      <span class="step-badge" :class="`badge-${message.type}`">
        {{ typeLabel }}
      </span>
      <span class="step-id">{{ message.id }}</span>
    </div>

    <div class="step-content">
      <!-- 实时 Markdown 渲染（直接 v-html，简单场景足够） -->
      <div
        v-if="message.content"
        class="markdown-body"
        v-html="renderedMd"
      />
      <!-- 流式光标（正在生成时显示） -->
      <span v-if="isCurrentStep && isRunning" class="typing-cursor">▌</span>
      <!-- 空内容占位 -->
      <div v-if="!message.content && !isCurrentStep" class="empty-content">
        <span class="text-muted">（空）</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { StepMessage } from '@/stores/workflow'

const props = defineProps<{
  message: StepMessage
  isCurrentStep: boolean
  isRunning: boolean
}>()

const typeLabel = computed(() => {
  const map: Record<string, string> = {
    text: '📝 文本',
    fixed: '📌 固定',
    table: '📊 表格',
    pic: '🖼 图表',
    rag: '🔍 RAG',
    classifier: '⚡ 分类',
  }
  return map[props.message.type] || props.message.type
})

/**
 * 轻量级 Markdown → HTML 渲染
 * 支持标题、粗体、斜体、代码块、表格、列表等
 * 生产项目中可换 marked.js 或 markdown-it
 */
const renderedMd = computed(() => {
  let md = props.message.content
  if (!md) return ''

  // 代码块
  md = md.replace(/```[\w]*\n?([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
  // 行内代码
  md = md.replace(/`([^`]+)`/g, '<code>$1</code>')
  // 标题
  md = md.replace(/^#### (.+)$/gm, '<h4>$1</h4>')
  md = md.replace(/^### (.+)$/gm, '<h3>$1</h3>')
  md = md.replace(/^## (.+)$/gm, '<h2>$1</h2>')
  md = md.replace(/^# (.+)$/gm, '<h1>$1</h1>')
  // 粗体 / 斜体
  md = md.replace(/\*\*\*(.+?)\*\*\*/g, '<strong><em>$1</em></strong>')
  md = md.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  md = md.replace(/\*(.+?)\*/g, '<em>$1</em>')
  // 无序列表
  md = md.replace(/^[-*] (.+)$/gm, '<li>$1</li>')
  md = md.replace(/(<li>[\s\S]+?<\/li>)/g, '<ul>$1</ul>')
  // 有序列表
  md = md.replace(/^\d+\. (.+)$/gm, '<li>$1</li>')
  // 水平线
  md = md.replace(/^---$/gm, '<hr/>')
  // 换行
  md = md.replace(/\n\n/g, '</p><p>')
  md = md.replace(/\n/g, '<br/>')

  return '<p>' + md + '</p>'
})
</script>

<style scoped>
.wf-step-card {
  background: linear-gradient(135deg, rgba(18, 22, 36, 0.8), rgba(12, 15, 25, 0.92));
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;
  backdrop-filter: blur(12px);
  transition: border-color 0.25s;
}

.wf-step-card.thinking {
  border-color: rgba(99, 102, 241, 0.35);
  background: linear-gradient(135deg, rgba(14, 12, 36, 0.8), rgba(12, 10, 28, 0.92));
}

.step-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
}

.step-badge {
  font-size: 0.75rem;
  padding: 2px 10px;
  border-radius: 20px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.badge-text    { background: rgba(122, 162, 255, 0.2); color: #7aa2ff; border: 1px solid rgba(122, 162, 255, 0.4); }
.badge-fixed   { background: rgba(34, 197, 94, 0.15);  color: #22c55e; border: 1px solid rgba(34, 197, 94, 0.35); }
.badge-table   { background: rgba(245, 158, 11, 0.15); color: #f59e0b; border: 1px solid rgba(245, 158, 11, 0.35); }
.badge-pic     { background: rgba(167, 139, 250, 0.2); color: #a78bfa; border: 1px solid rgba(167, 139, 250, 0.4); }
.badge-rag     { background: rgba(56, 189, 248, 0.15); color: #38bdf8; border: 1px solid rgba(56, 189, 248, 0.35); }
.badge-classifier { background: rgba(239, 68, 68, 0.15); color: #ef4444; border: 1px solid rgba(239, 68, 68, 0.35); }

.step-id {
  font-size: 0.72rem;
  color: rgba(154, 163, 178, 0.6);
  font-family: monospace;
}

.step-content {
  line-height: 1.7;
  font-size: 0.95rem;
  color: rgba(230, 233, 239, 0.9);
  word-break: break-word;
}

.typing-cursor {
  display: inline-block;
  color: #7aa2ff;
  animation: blink 0.9s step-end infinite;
  font-size: 1rem;
  margin-left: 2px;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0; }
}

/* Markdown 样式 */
.markdown-body :deep(h1) { font-size: 1.5rem; font-weight: 700; margin: 12px 0 6px; color: #e6e9ef; }
.markdown-body :deep(h2) { font-size: 1.25rem; font-weight: 600; margin: 10px 0 6px; color: #d1d5db; }
.markdown-body :deep(h3) { font-size: 1.1rem;  font-weight: 600; margin: 8px 0 4px;  color: #c4c8d1; }
.markdown-body :deep(h4) { font-size: 1rem;    font-weight: 600; margin: 6px 0 4px;  color: #9aa3b2; }
.markdown-body :deep(p)  { margin: 6px 0; }
.markdown-body :deep(strong) { color: #7aa2ff; }
.markdown-body :deep(code) {
  background: rgba(122, 162, 255, 0.12);
  border: 1px solid rgba(122, 162, 255, 0.2);
  border-radius: 4px;
  padding: 1px 5px;
  font-size: 0.87rem;
  font-family: 'JetBrains Mono', monospace;
  color: #a5b4fc;
}
.markdown-body :deep(pre) {
  background: rgba(5, 8, 15, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 12px 16px;
  overflow-x: auto;
  margin: 8px 0;
}
.markdown-body :deep(pre code) {
  background: transparent;
  border: none;
  padding: 0;
  color: #c8d2f0;
}
.markdown-body :deep(ul), .markdown-body :deep(ol) {
  padding-left: 20px;
  margin: 6px 0;
}
.markdown-body :deep(li) { margin: 2px 0; }
.markdown-body :deep(hr) { border-color: rgba(255,255,255,0.1); margin: 12px 0; }
.markdown-body :deep(table) { width: 100%; border-collapse: collapse; margin: 8px 0; font-size: 0.9rem; }
.markdown-body :deep(th), .markdown-body :deep(td) {
  border: 1px solid rgba(255,255,255,0.12);
  padding: 6px 12px;
}
.markdown-body :deep(th) { background: rgba(122, 162, 255, 0.1); color: #7aa2ff; }
</style>

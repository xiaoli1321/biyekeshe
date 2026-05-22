<template>
  <div class="node-detail-panel" :class="{ show: visible }">
    <div v-if="node" class="card shadow border-0 h-100">
      <div class="card-header d-flex justify-content-between align-items-center"
           :class="headerClass">
        <h5 class="mb-0 text-truncate" style="max-width: 220px;" :title="node.name">
          {{ node.name }}
        </h5>
        <button class="btn-close btn-close-white" @click="$emit('close')"></button>
      </div>
      <div class="card-body overflow-auto">
        <!-- 基础信息 -->
        <div class="mb-3">
          <span class="badge me-1" :class="difficultyBadgeClass">
            {{ difficultyLabel }}
          </span>
          <span class="badge me-1" :class="progressBadgeClass">
            {{ progressLabel }}
          </span>
          <span v-if="node.category" class="badge bg-secondary me-1">
            {{ node.category }}
          </span>
        </div>

        <div class="content-card mb-3">
          <div class="content-card-label">一句话定义</div>
          <div class="content-card-text">
            {{ node.summary || context?.concept?.summary || node.description || context?.concept?.description || '暂无定义' }}
          </div>
        </div>

        <div v-if="node.chapterTitle" class="mb-3 small chapter-hint">
          <i class="bi bi-collection me-1"></i>
          这是“{{ node.chapterTitle }}”章节下的知识点节点，章节本身不是图谱节点。
        </div>

        <div v-if="node.content || context?.concept?.content" class="content-card mb-3">
          <div class="content-card-label">详细讲解</div>
          <div class="content-card-text pre-wrap">
            {{ node.content || context?.concept?.content }}
          </div>
        </div>

        <div v-if="node.example || context?.concept?.example" class="content-card mb-3 is-accent">
          <div class="content-card-label">示例</div>
          <div class="content-card-text pre-wrap">
            {{ node.example || context?.concept?.example }}
          </div>
        </div>

        <div v-if="node.commonPitfall || context?.concept?.commonPitfall" class="content-card mb-3 is-warning">
          <div class="content-card-label">常见误区</div>
          <div class="content-card-text pre-wrap">
            {{ node.commonPitfall || context?.concept?.commonPitfall }}
          </div>
        </div>

        <div v-if="node.chapterTitle" class="mb-2 small">
          <i class="bi bi-book me-1"></i>
          所属章节: {{ node.chapterTitle }}
        </div>

        <div class="mb-2 small">
          <i class="bi bi-bar-chart me-1"></i>
          重要程度: {{ node.importanceWeight }}/100
        </div>

        <hr>

        <!-- 进度操作 -->
        <div class="mb-3">
          <label class="form-label small fw-bold">掌握状态</label>
          <div class="status-grid">
            <button
              v-for="option in progressOptions"
              :key="option.value"
              type="button"
              class="status-card"
              :class="[option.className, { active: node.progressStatus === option.value }]"
              @click="updateProgress(option.value)"
            >
              <div class="status-card-title">{{ option.label }}</div>
              <div class="status-card-desc">{{ option.desc }}</div>
            </button>
          </div>
        </div>

        <hr>

        <!-- 先修知识点 -->
        <div v-if="context?.prerequisites?.length" class="mb-3">
          <h6 class="small fw-bold text-primary">
            <i class="bi bi-arrow-left-circle me-1"></i>先修知识点 ({{ context.prerequisites.length }})
          </h6>
          <ul class="list-unstyled small mb-0">
            <li v-for="p in context.prerequisites" :key="p.id" class="mb-1">
              <a href="#" class="text-decoration-none" @click.prevent="$emit('navigateTo', p.id)">
                {{ p.name }}
              </a>
              <span class="badge bg-light text-dark ms-1" style="font-size: 0.65rem;">
                Lv.{{ p.difficultyLevel }}
              </span>
            </li>
          </ul>
        </div>

        <!-- 后续知识点 -->
        <div v-if="context?.dependents?.length" class="mb-3">
          <h6 class="small fw-bold text-success">
            <i class="bi bi-arrow-right-circle me-1"></i>后续知识点 ({{ context.dependents.length }})
          </h6>
          <ul class="list-unstyled small mb-0">
            <li v-for="d in context.dependents" :key="d.id" class="mb-1">
              <a href="#" class="text-decoration-none" @click.prevent="$emit('navigateTo', d.id)">
                {{ d.name }}
              </a>
              <span class="badge bg-light text-dark ms-1" style="font-size: 0.65rem;">
                Lv.{{ d.difficultyLevel }}
              </span>
            </li>
          </ul>
        </div>

        <!-- 关联知识点 -->
        <div v-if="context?.related?.length">
          <h6 class="small fw-bold text-info">
            <i class="bi bi-link-45deg me-1"></i>关联知识点 ({{ context.related.length }})
          </h6>
          <ul class="list-unstyled small mb-0">
            <li v-for="r in context.related" :key="r.id" class="mb-1">
              <a href="#" class="text-decoration-none" @click.prevent="$emit('navigateTo', r.id)">
                {{ r.name }}
              </a>
            </li>
          </ul>
        </div>
      </div>

      <!-- 底部操作 -->
      <div class="card-footer">
        <div class="d-grid gap-2">
          <router-link
            v-if="context?.courseId"
            :to="`/courses/${context.courseId}`"
            class="btn btn-sm btn-outline-primary"
          >
            <i class="bi bi-play-circle me-1"></i>进入课程
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { GraphNode, ConceptContext } from '@/services/api/graph'

const props = defineProps<{
  visible: boolean
  node: GraphNode | null
  context: ConceptContext | null
}>()

const emit = defineEmits<{
  close: []
  navigateTo: [conceptId: string]
  progressChange: [conceptId: string, status: string]
}>()

const headerClass = computed(() => {
  const level = props.node?.difficultyLevel || 1
  if (level <= 2) return 'bg-success text-white'
  if (level <= 4) return 'bg-warning text-dark'
  return 'bg-danger text-white'
})

const difficultyLabel = computed(() => {
  const level = props.node?.difficultyLevel || 1
  if (level <= 2) return '基础'
  if (level <= 4) return '进阶'
  return '高级'
})

const difficultyBadgeClass = computed(() => {
  const level = props.node?.difficultyLevel || 1
  if (level <= 2) return 'bg-success'
  if (level <= 4) return 'bg-warning text-dark'
  return 'bg-danger'
})

const progressLabel = computed(() => {
  const status = props.node?.progressStatus
  if (status === 'COMPLETED') return '已完成'
  if (status === 'MASTERED') return '已掌握'
  if (status === 'IN_PROGRESS') return '进行中'
  return '未开始'
})

const progressBadgeClass = computed(() => {
  const status = props.node?.progressStatus
  if (status === 'COMPLETED') return 'bg-primary'
  if (status === 'MASTERED') return 'bg-success'
  if (status === 'IN_PROGRESS') return 'bg-info'
  return 'bg-light text-dark'
})

const progressOptions = [
  { value: 'NOT_STARTED', label: '未开始', desc: '还没进入学习', className: 'is-not-started' },
  { value: 'IN_PROGRESS', label: '进行中', desc: '正在学习中', className: 'is-in-progress' },
  { value: 'COMPLETED', label: '已完成', desc: '已经学完', className: 'is-completed' },
  { value: 'MASTERED', label: '已掌握', desc: '可以熟练运用', className: 'is-mastered' }
] as const

function updateProgress(status: string) {
  if (props.node) {
    emit('progressChange', props.node.id, status)
  }
}
</script>

<style scoped>
.node-detail-panel {
  position: fixed;
  top: 80px;
  right: -340px;
  width: 340px;
  max-height: calc(100vh - 120px);
  z-index: 1050;
  transition: right 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.node-detail-panel.show {
  right: 20px;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.5rem;
}

.status-card {
  border: 1px solid #dee2e6;
  border-radius: 0.75rem;
  background: #fff;
  text-align: left;
  padding: 0.75rem;
  transition: all 0.2s ease;
}

.status-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 0.35rem 0.9rem rgba(0, 0, 0, 0.08);
}

.status-card.active {
  border-width: 2px;
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.12);
}

.status-card-title {
  font-size: 0.9rem;
  font-weight: 700;
}

.status-card-desc {
  font-size: 0.75rem;
  color: #6c757d;
  margin-top: 0.25rem;
}

.status-card.is-not-started.active {
  border-color: #6c757d;
  background: #f8f9fa;
}

.status-card.is-in-progress.active {
  border-color: #fd7e14;
  background: #fff3e8;
}

.status-card.is-completed.active {
  border-color: #0d6efd;
  background: #e9f2ff;
}

.status-card.is-mastered.active {
  border-color: #198754;
  background: #eaf7ef;
}

.content-card {
  padding: 0.8rem 0.85rem;
  border-radius: 0.9rem;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.content-card.is-accent {
  background: #eff6ff;
  border-color: #bfdbfe;
}

.content-card.is-warning {
  background: #fff7ed;
  border-color: #fed7aa;
}

.content-card-label {
  font-size: 0.74rem;
  font-weight: 700;
  color: #475569;
  margin-bottom: 0.35rem;
}

.content-card-text {
  font-size: 0.82rem;
  line-height: 1.55;
  color: #0f172a;
}

.chapter-hint {
  padding: 0.7rem 0.8rem;
  border-radius: 0.85rem;
  background: #f1f5f9;
  color: #475569;
}

.pre-wrap {
  white-space: pre-wrap;
}
</style>

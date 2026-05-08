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

        <p class="text-muted small">{{ node.description || '暂无描述信息' }}</p>

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
          <select class="form-select form-select-sm" :value="node.progressStatus" @change="onProgressChange">
            <option value="NOT_STARTED">未开始</option>
            <option value="IN_PROGRESS">标记进行中</option>
            <option value="COMPLETED">标记已完成</option>
            <option value="MASTERED">标记已掌握</option>
          </select>
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

function onProgressChange(e: Event) {
  const target = e.target as HTMLSelectElement
  if (props.node) {
    emit('progressChange', props.node.id, target.value)
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
</style>

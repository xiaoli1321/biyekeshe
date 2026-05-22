<template>
  <div class="filter-panel card shadow-sm border-0 mb-3">
    <div class="card-body py-2 px-3">
      <div class="row g-2 align-items-center">
        <div class="col-auto">
          <span class="text-muted small fw-bold">筛选:</span>
        </div>
        <div class="col-auto">
          <select class="form-select form-select-sm" v-model="localDifficulty" @change="emitFilters">
            <option value="">全部难度</option>
            <option value="1-2">基础 (1-2)</option>
            <option value="3-4">进阶 (3-4)</option>
            <option value="5">高级 (5)</option>
          </select>
        </div>
        <div class="col-auto">
          <select class="form-select form-select-sm" v-model="localProgress" @change="emitFilters">
            <option value="">全部状态</option>
            <option value="NOT_STARTED">未开始</option>
            <option value="IN_PROGRESS">进行中</option>
            <option value="COMPLETED">已完成</option>
            <option value="MASTERED">已掌握</option>
          </select>
        </div>
        <div class="col-auto">
          <select class="form-select form-select-sm" v-model="localRelationType" @change="emitFilters">
            <option value="">全部关系</option>
            <option value="PREREQUISITE">先修关系</option>
            <option value="DEPENDS_ON">依赖关系</option>
            <option value="SIMILAR_TO">相似关系</option>
            <option value="PART_OF">包含关系</option>
            <option value="USES">使用关系</option>
          </select>
        </div>
        <div class="col-auto">
          <input
            v-model.trim="localSearchTerm"
            type="text"
            class="form-control form-control-sm"
            placeholder="搜索知识点"
            @input="emitFilters"
          >
        </div>
        <div class="col-auto">
          <button
            class="btn btn-sm"
            :class="highlightPathMode ? 'btn-warning' : 'btn-outline-warning'"
            @click="togglePathMode"
            title="依次点击两个节点以标注最短路径"
          >
            <i class="bi bi-signpost me-1"></i>
            {{ highlightPathMode ? '路径模式 (开)' : '路径模式' }}
          </button>
        </div>
        <div class="col-auto" v-if="pathNodes.length > 0">
          <span class="badge bg-warning text-dark">
            已选 {{ pathNodes.length }}/2 个节点
          </span>
        </div>
        <div class="col-auto ms-auto">
          <button class="btn btn-sm btn-outline-success me-1" @click="$emit('showLearningPath')" title="显示推荐学习路径">
            <i class="bi bi-map me-1"></i>学习路径
          </button>
          <button class="btn btn-sm btn-outline-danger" @click="$emit('resetAll')" title="重置所有筛选和选择">
            <i class="bi bi-arrow-counterclockwise"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  difficulty: string
  progress: string
  relationType: string
  searchTerm: string
  highlightPathMode: boolean
  pathNodes: string[]
}>()

const emit = defineEmits<{
  'update:difficulty': [value: string]
  'update:progress': [value: string]
  'update:relationType': [value: string]
  'update:searchTerm': [value: string]
  'update:highlightPathMode': [value: boolean]
  'showLearningPath': []
  'resetAll': []
}>()

const localDifficulty = ref(props.difficulty)
const localProgress = ref(props.progress)
const localRelationType = ref(props.relationType)
const localSearchTerm = ref(props.searchTerm)
const highlightPathMode = ref(props.highlightPathMode)

watch(() => props.difficulty, (v) => { localDifficulty.value = v })
watch(() => props.progress, (v) => { localProgress.value = v })
watch(() => props.relationType, (v) => { localRelationType.value = v })
watch(() => props.searchTerm, (v) => { localSearchTerm.value = v })
watch(() => props.highlightPathMode, (v) => { highlightPathMode.value = v })

function emitFilters() {
  emit('update:difficulty', localDifficulty.value)
  emit('update:progress', localProgress.value)
  emit('update:relationType', localRelationType.value)
  emit('update:searchTerm', localSearchTerm.value)
}

function togglePathMode() {
  highlightPathMode.value = !highlightPathMode.value
  emit('update:highlightPathMode', highlightPathMode.value)
}
</script>

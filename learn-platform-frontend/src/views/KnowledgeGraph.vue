<template>
  <div class="container">
    <div class="row mb-4">
      <div class="col-12">
        <h1 class="display-6">
          <i class="bi bi-diagram-3 me-2"></i>
          知识图谱
        </h1>
        <p class="lead text-muted">
          可视化展示课程之间的关联关系和学习路径
        </p>
      </div>
    </div>

    <!-- Graph Controls -->
    <div class="row mb-4">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <div class="row align-items-center">
              <div class="col-md-6">
                <div class="input-group">
                  <span class="input-group-text">
                    <i class="bi bi-search"></i>
                  </span>
                  <input
                    type="text"
                    class="form-control"
                    placeholder="搜索课程或概念..."
                    v-model="searchTerm"
                    @input="handleSearch"
                  />
                </div>
              </div>
              <div class="col-md-6 text-end">
                <button class="btn btn-outline-primary me-2" @click="resetZoom">
                  <i class="bi bi-zoom-in me-1"></i>
                  重置视图
                </button>
                <button class="btn btn-outline-secondary" @click="toggleLayout">
                  <i class="bi bi-layout-text-window me-1"></i>
                  切换布局
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Graph Visualization -->
    <div class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-body p-0">
            <div
              id="knowledge-graph"
              style="height: 600px; position: relative; background: #f8f9fa;"
            >
              <Loading v-if="loading" />

              <!-- SVG Graph Container -->
              <div v-if="!loading" class="graph-container">
                <svg id="graph-svg" width="100%" height="600">
                  <!-- Graph will be rendered here -->
                </svg>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Graph Legend -->
    <div class="row mt-4">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h6 class="mb-3">图例说明</h6>
            <div class="row">
              <div class="col-md-3">
                <div class="d-flex align-items-center">
                  <div class="graph-node bg-primary me-2"></div>
                  <span>课程</span>
                </div>
              </div>
              <div class="col-md-3">
                <div class="d-flex align-items-center">
                  <div class="graph-node bg-success me-2"></div>
                  <span>已完成</span>
                </div>
              </div>
              <div class="col-md-3">
                <div class="d-flex align-items-center">
                  <div class="graph-node bg-warning me-2"></div>
                  <span>进行中</span>
                </div>
              </div>
              <div class="col-md-3">
                <div class="d-flex align-items-center">
                  <div class="me-2">
                    <svg width="20" height="20">
                      <line x1="0" y1="10" x2="20" y2="10" stroke="#666" stroke-width="2" />
                    </svg>
                  </div>
                  <span>依赖关系</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Node Details Panel -->
    <div
      v-if="selectedNode"
      class="node-details-panel"
      :style="{ transform: `translateX(${showDetails ? '0' : '100%'})` }"
    >
      <div class="card">
        <div class="card-header bg-white">
          <div class="d-flex justify-content-between align-items-center">
            <h5 class="mb-0">{{ selectedNode.title }}</h5>
            <button class="btn-close" @click="closeDetails"></button>
          </div>
        </div>
        <div class="card-body">
          <p><strong>类型:</strong> {{ selectedNode.type }}</p>
          <p><strong>难度:</strong> {{ selectedNode.difficulty || '未知' }}</p>
          <p><strong>描述:</strong> {{ selectedNode.description }}</p>
          <router-link
            v-if="selectedNode.type === 'course'"
            :to="`/courses/${selectedNode.id}`"
            class="btn btn-primary"
          >
            查看课程
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

const loading = ref(false)
const searchTerm = ref('')
const showDetails = ref(false)
const selectedNode = ref(null)
const forceLayout = ref('force')

// Mock graph data - in real app this would come from API
const graphData = computed(() => ({
  nodes: [
    {
      id: '1',
      title: 'JavaScript 基础',
      type: 'course',
      difficulty: '初级',
      description: 'JavaScript编程语言入门课程'
    },
    {
      id: '2',
      title: 'Vue 3 框架',
      type: 'course',
      difficulty: '中级',
      description: 'Vue 3 渐进式框架学习'
    },
    {
      id: '3',
      title: '前端工程化',
      type: 'course',
      difficulty: '高级',
      description: '现代前端开发工程化实践'
    }
  ],
  links: [
    {
      source: '1',
      target: '2',
      type: 'prerequisite'
    },
    {
      source: '2',
      target: '3',
      type: 'prerequisite'
    }
  ]
}))

const handleSearch = () => {
  // Implement search logic
  console.log('Searching for:', searchTerm.value)
}

const resetZoom = () => {
  // Reset graph zoom
  console.log('Resetting zoom')
}

const toggleLayout = () => {
  forceLayout.value = forceLayout.value === 'force' ? 'circular' : 'force'
  // Re-render graph with new layout
}

const closeDetails = () => {
  showDetails.value = false
  selectedNode.value = null
}

onMounted(async () => {
  loading.value = true

  // Simulate API call
  await new Promise(resolve => setTimeout(resolve, 1000))

  // In a real implementation, you would use a library like D3.js or vis.js
  // to render the interactive graph
  console.log('Knowledge graph data:', graphData.value)

  loading.value = false
})
</script>

<style scoped>
.graph-container {
  position: relative;
  width: 100%;
  height: 600px;
}

.graph-node {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: inline-block;
}

.node-details-panel {
  position: fixed;
  top: 100px;
  right: 20px;
  width: 300px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s ease;
  z-index: 1000;
}

.node-details-panel .card {
  border: none;
  border-radius: 8px;
}

.card-header {
  border-bottom: 1px solid #dee2e6;
}
</style>
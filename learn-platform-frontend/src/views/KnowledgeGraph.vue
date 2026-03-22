<template>
  <div class="container">
    <div class="row mb-4">
      <div class="col-12">
        <h1 class="display-6">
          <i class="bi bi-diagram-3 me-2"></i>
          知识图谱
        </h1>
        <p class="lead text-muted">
          可视化展示课程与章节的网状结构
        </p>
      </div>
    </div>

    <!-- Graph Controls -->
    <div class="row mb-4">
      <div class="col-12">
        <div class="card shadow-sm border-0">
          <div class="card-body">
            <div class="row align-items-center">
              <div class="col-md-6">
                <div class="input-group">
                  <span class="input-group-text bg-transparent border-end-0">
                    <i class="bi bi-search"></i>
                  </span>
                  <input
                    type="text"
                    class="form-control border-start-0 ps-0"
                    placeholder="搜索课程或章节名称..."
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
                  切换布局 ({{ forceLayout === 'force' ? '力导向' : '环形' }})
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
        <div class="card shadow-sm border-0 overflow-hidden">
          <div class="card-body p-0 position-relative">
            <div v-if="loading" class="position-absolute w-100 h-100 d-flex justify-content-center align-items-center" style="background: rgba(255,255,255,0.8); z-index: 10;">
              <Loading />
            </div>
            
            <div
              ref="chartContainer"
              class="w-100"
              style="height: 700px; background-color: #f8f9fa;"
            ></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Graph Legend -->
    <div class="row mt-4">
      <div class="col-12">
        <div class="card shadow-sm border-0">
          <div class="card-body">
            <h6 class="mb-3 fw-bold">图例说明</h6>
            <div class="row text-center">
              <div class="col-md-4">
                <div class="d-flex align-items-center justify-content-center">
                  <div class="graph-node bg-primary me-2"></div>
                  <span>核心课程节点</span>
                </div>
              </div>
              <div class="col-md-4">
                <div class="d-flex align-items-center justify-content-center">
                  <div class="graph-node me-2" style="background-color: #28a745;"></div>
                  <span>课程章节节点</span>
                </div>
              </div>
              <div class="col-md-4">
                <div class="d-flex align-items-center justify-content-center">
                  <div class="me-2 d-flex align-items-center">
                    <svg width="40" height="20">
                      <line x1="0" y1="10" x2="30" y2="10" stroke="#999" stroke-width="2" />
                      <polygon points="30,5 40,10 30,15" fill="#999" />
                    </svg>
                  </div>
                  <span>包含关系</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Node Details Panel -->
    <div
      class="node-details-panel"
      :class="{ 'show-panel': showDetails }"
    >
      <div v-if="selectedNode" class="card shadow border-0 h-100">
        <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
          <h5 class="mb-0 text-truncate" style="max-width: 200px;" :title="selectedNode.title">{{ selectedNode.title }}</h5>
          <button class="btn-close btn-close-white" @click="closeDetails"></button>
        </div>
        <div class="card-body">
          <div class="mb-3">
            <span class="badge bg-secondary me-2">{{ selectedNode.type === 'course' ? '课程' : '章节' }}</span>
            <span v-if="selectedNode.type === 'course'" class="badge" :class="selectedNode.difficulty === 'BEGINNER' ? 'bg-success' : selectedNode.difficulty === 'INTERMEDIATE' ? 'bg-warning text-dark' : 'bg-danger'">
              难度: {{ selectedNode.difficulty === 'BEGINNER' ? '初级' : selectedNode.difficulty === 'INTERMEDIATE' ? '中级' : '高级' }}
            </span>
            <span v-if="selectedNode.type === 'chapter'" class="badge bg-info">
              类型: {{ selectedNode.chapterType === 'VIDEO' ? '视频' : '文本' }}
            </span>
          </div>
          <p class="text-muted">{{ selectedNode.description || '暂无描述信息' }}</p>
          <p v-if="selectedNode.type === 'chapter'" class="text-muted small">
            <i class="bi bi-clock"></i> 预计时长: {{ selectedNode.estimatedMinutes || 0 }} 分钟
          </p>
          
          <hr>
          
          <div class="d-grid gap-2 mt-4">
            <router-link
              v-if="selectedNode.type === 'course'"
              :to="`/courses/${selectedNode.courseId || selectedNode.id}`"
              class="btn btn-primary"
            >
              <i class="bi bi-play-circle me-1"></i> 立即学习课程
            </router-link>
            <router-link
              v-else
              :to="`/chapters/${selectedNode.id}`"
              class="btn btn-outline-primary"
            >
               进入章节内容
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, shallowRef, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import Loading from '@/components/Loading.vue'
import { useCourseStore } from '@/stores/course'

const courseStore = useCourseStore()

const loading = ref(true)
const searchTerm = ref('')
const showDetails = ref(false)
const selectedNode = ref<any>(null)
const forceLayout = ref('force')

const chartContainer = ref<HTMLElement | null>(null)
const chartInstance = shallowRef<echarts.ECharts | null>(null)

const graphData = ref<{ nodes: any[], links: any[] }>({ nodes: [], links: [] })

const loadGraphData = async () => {
  loading.value = true
  const nodes: any[] = []
  const links: any[] = []

  // Load actual courses
  const response = await courseStore.fetchAllCourses()
  const courses = courseStore.courses

  if (!courses || courses.length === 0) {
    graphData.value = { nodes, links }
    loading.value = false
    initChart()
    return
  }

  // Iterate over courses to construct nodes and edges
  for (const course of courses) {
    const courseAny = course as any
    const cId = courseAny.id
    
    // Core Course Node
    nodes.push({
      id: cId,
      title: courseAny.title || courseAny.name || '未知课程标题',
      type: 'course',
      difficulty: courseAny.difficultyLevel || 'BEGINNER',
      description: courseAny.description,
      courseId: cId
    })

    // Fetch chapters sequentially
    const chaptersResult = await courseStore.fetchCourseChapters(cId)
    if (chaptersResult.success && chaptersResult.data) {
      const chapters = chaptersResult.data
      chapters.forEach((chap: any, index: number) => {
        nodes.push({
          id: chap.id,
          title: chap.title,
          type: 'chapter',
          description: chap.description,
          chapterType: chap.type,
          estimatedMinutes: chap.estimatedMinutes,
          courseId: cId
        })

        links.push({
          source: cId,
          target: chap.id,
          type: 'contains'
        })
      })
    }
  }

  graphData.value = { nodes, links }
  loading.value = false
  initChart()
}

const initChart = () => {
  if (!chartContainer.value) return
  
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }
  
  const chart = echarts.init(chartContainer.value)
  chartInstance.value = chart
  
  // Apply Search Highlight Mechanism
  const processedNodes = graphData.value.nodes.map(node => {
    let color = node.type === 'course' ? '#0d6efd' : '#28a745'
    
    // Dim if search text does not match
    const isMuted = searchTerm.value && !node.title.toLowerCase().includes(searchTerm.value.toLowerCase())
    
    return {
      id: node.id,
      name: node.title,
      symbolSize: node.type === 'course' ? 65 : 40,
      itemStyle: { 
        color,
        opacity: isMuted ? 0.2 : 0.9,
        borderColor: '#fff',
        borderWidth: node.type === 'course' ? 3 : 2,
        shadowBlur: isMuted ? 0 : 8,
        shadowColor: 'rgba(0,0,0,0.15)'
      },
      label: {
        show: node.type === 'course' || (!searchTerm.value || !isMuted),
        color: isMuted ? '#ccc' : '#333'
      },
      ...node
    }
  })

  const processedLinks = graphData.value.links.map(link => {
    // Check if the link connects to a matching node during search
    const isSourceMatch = !searchTerm.value || graphData.value.nodes.find(n => n.id === link.source)?.title.toLowerCase().includes(searchTerm.value.toLowerCase());
    const isTargetMatch = !searchTerm.value || graphData.value.nodes.find(n => n.id === link.target)?.title.toLowerCase().includes(searchTerm.value.toLowerCase());
    const isMuted = searchTerm.value && !(isSourceMatch || isTargetMatch);

    return {
      source: link.source,
      target: link.target,
      label: {
        show: false
      },
      lineStyle: {
        width: 2,
        curveness: 0.15,
        color: '#adb5bd',
        opacity: isMuted ? 0.1 : 0.7
      }
    }
  })
  
  const option = {
    tooltip: {
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e9ecef',
      textStyle: { color: '#333' },
      formatter: function (params: any) {
        if (params.dataType === 'node') {
          return `
            <div style="font-weight:bold; margin-bottom:5px;">${params.data.name}</div>
            <div style="font-size:12px; color:#666; max-width: 200px; white-space: normal;">
              ${params.data.description || '暂无详细描述'}
            </div>
            <div style="margin-top:5px; font-size:12px; color:${params.data.type === 'course' ? '#0d6efd' : '#28a745'};">
              节点类型：${params.data.type === 'course' ? '课程' : '章节'}
            </div>
          `
        }
        return ''
      }
    },
    animationDurationUpdate: 1500,
    animationEasingUpdate: 'quinticInOut',
    series: [
      {
        type: 'graph',
        layout: forceLayout.value, // 'force' or 'circular'
        data: processedNodes,
        links: processedLinks,
        roam: true, // Allow zooming and dragging
        label: {
          show: true,
          position: 'right',
          formatter: '{b}',
          fontSize: 12,
          fontWeight: 'bold'
        },
        edgeSymbol: ['circle', 'arrow'],
        edgeSymbolSize: [4, 8],
        force: {
          repulsion: 800,
          edgeLength: [80, 150],
          gravity: 0.05,
          layoutAnimation: true
        },
        focusNodeAdjacency: true,
        emphasis: {
          focus: 'adjacency',
          lineStyle: {
            width: 4
          }
        }
      }
    ]
  }
  
  chart.setOption(option)
  
  chart.on('click', (params) => {
    if (params.dataType === 'node') {
      const node = graphData.value.nodes.find(n => n.id === params.data.id)
      if (node) {
        selectedNode.value = node
        showDetails.value = true
      }
    } else {
      showDetails.value = false
    }
  })
  
  // Click empty space to close panel
  chart.getZr().on('click', (event) => {
    if (!event.target) {
      showDetails.value = false
    }
  })
}

let searchTimeout: any;
const handleSearch = () => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    if (chartInstance.value) {
      initChart()
    }
  }, 300)
}

const resetZoom = () => {
  searchTerm.value = ''
  if (chartInstance.value) {
    initChart()
  }
}

const toggleLayout = () => {
  forceLayout.value = forceLayout.value === 'force' ? 'circular' : 'force'
  if (chartInstance.value) {
    initChart()
  }
}

const closeDetails = () => {
  showDetails.value = false
}

const handleResize = () => {
  if (chartInstance.value) {
    chartInstance.value.resize()
  }
}

onMounted(async () => {
  await loadGraphData()
  nextTick(() => {
    window.addEventListener('resize', handleResize)
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance.value?.dispose()
})
</script>

<style scoped>
.graph-node {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: inline-block;
}

.node-details-panel {
  position: fixed;
  top: 80px;
  right: -320px; /* Initially hidden */
  width: 320px;
  height: calc(100vh - 120px);
  z-index: 1050;
  transition: right 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.node-details-panel.show-panel {
  right: 20px;
}
</style>
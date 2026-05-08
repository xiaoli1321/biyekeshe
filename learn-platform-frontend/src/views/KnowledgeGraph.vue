<template>
  <div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="row mb-3">
      <div class="col-12 d-flex align-items-center justify-content-between">
        <div>
          <h1 class="display-6 mb-0">
            <i class="bi bi-diagram-3 me-2"></i>知识图谱
          </h1>
          <p class="text-muted small mb-0 mt-1">
            {{ statsText }}
          </p>
        </div>
        <div class="d-flex align-items-center gap-2">
          <select class="form-select form-select-sm" style="width: 220px;" v-model="selectedCourseId" @change="loadGraph">
            <option value="">-- 选择课程 --</option>
            <option v-for="c in courses" :key="c.id" :value="c.id">{{ c.name || c.title }}</option>
          </select>
          <div class="btn-group btn-group-sm">
            <button class="btn" :class="layoutMode === 'force' ? 'btn-primary' : 'btn-outline-primary'" @click="setLayout('force')" title="力导向布局">
              <i class="bi bi-bounding-box-circles"></i>
            </button>
            <button class="btn" :class="layoutMode === 'circular' ? 'btn-primary' : 'btn-outline-primary'" @click="setLayout('circular')" title="环形布局">
              <i class="bi bi-circle"></i>
            </button>
          </div>
          <button class="btn btn-sm btn-outline-secondary" @click="resetView" title="重置视图">
            <i class="bi bi-zoom-in"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- Filter Panel -->
    <GraphFilterPanel
      v-model:difficulty="filterDifficulty"
      v-model:progress="filterProgress"
      v-model:relationType="filterRelationType"
      v-model:highlightPathMode="highlightPathMode"
      :pathNodes="pathNodes"
      @showLearningPath="showLearningPath"
      @resetAll="resetAll"
    />

    <!-- Graph Area -->
    <div class="row">
      <div class="col-12">
        <div class="card shadow-sm border-0 overflow-hidden">
          <div class="card-body p-0 position-relative">
            <div v-if="loading" class="position-absolute w-100 h-100 d-flex justify-content-center align-items-center" style="background: rgba(255,255,255,0.8); z-index: 10;">
              <Loading />
            </div>
            <div v-if="!selectedCourseId && !loading" class="d-flex justify-content-center align-items-center text-muted" style="height: 700px;">
              <div class="text-center">
                <i class="bi bi-arrow-up-circle" style="font-size: 3rem;"></i>
                <p class="mt-2">请选择一门课程以加载知识图谱</p>
              </div>
            </div>
            <div
              v-show="selectedCourseId"
              ref="chartContainer"
              class="w-100 position-relative"
              style="height: 700px; background-color: #f8f9fa;"
            >
              <GraphMiniMap
                :visible="graphData.nodes.length > 0"
                :totalNodes="graphData.nodes.length"
                :totalLinks="graphData.links.length"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Context Menu -->
    <div
      v-if="contextMenu.visible"
      class="context-menu card shadow border-0"
      :style="{ position: 'fixed', left: contextMenu.x + 'px', top: contextMenu.y + 'px', zIndex: 2000 }"
    >
      <div class="list-group list-group-flush">
        <button class="list-group-item list-group-item-action" @click="onContextMarkMastered">
          <i class="bi bi-check-circle text-success me-2"></i>标记已掌握
        </button>
        <button class="list-group-item list-group-item-action" @click="onContextSetTarget">
          <i class="bi bi-bullseye text-warning me-2"></i>{{ highlightPathMode ? '设为路径起点' : '查看详情' }}
        </button>
        <button class="list-group-item list-group-item-action" @click="onContextShowPrereqs">
          <i class="bi bi-diagram-2 text-info me-2"></i>高亮先修链
        </button>
        <hr class="dropdown-divider my-0">
        <button class="list-group-item list-group-item-action text-danger" @click="contextMenu.visible = false">
          <i class="bi bi-x-circle me-2"></i>关闭菜单
        </button>
      </div>
    </div>

    <!-- Node Detail Panel -->
    <GraphNodeDetail
      :visible="showDetail"
      :node="selectedNode"
      :context="conceptContext"
      @close="showDetail = false"
      @navigateTo="navigateToConcept"
      @progressChange="onProgressChange"
    />

    <!-- Learning Path Modal -->
    <div class="modal fade" ref="learningPathModal" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header bg-primary text-white">
            <h5 class="modal-title"><i class="bi bi-map me-2"></i>推荐学习路径</h5>
            <button class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3 small text-muted">
              基于拓扑排序的最优学习路径，先修知识点在前，同级按难度和重要性排列
            </div>
            <div v-if="learningPath.length === 0" class="text-center text-muted py-5">
              暂无学习路径数据
            </div>
            <div class="list-group" v-else>
              <div
                v-for="(item, idx) in learningPath"
                :key="item.id"
                class="list-group-item d-flex align-items-center"
                :class="{ 'list-group-item-success': item._progressStatus === 'COMPLETED' }"
              >
                <span class="badge bg-primary rounded-pill me-3">{{ idx + 1 }}</span>
                <div class="flex-grow-1">
                  <strong>{{ item.name }}</strong>
                  <span class="badge ms-2" :class="item.difficultyLevel <= 2 ? 'bg-success' : item.difficultyLevel <= 4 ? 'bg-warning text-dark' : 'bg-danger'">
                    Lv.{{ item.difficultyLevel }}
                  </span>
                </div>
                <span v-if="item._progressStatus === 'COMPLETED'" class="badge bg-success">已完成</span>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary btn-sm" data-bs-dismiss="modal">关闭</button>
            <button class="btn btn-primary btn-sm" @click="highlightLearningPathOnGraph">
              <i class="bi bi-eye me-1"></i>在图上高亮
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, shallowRef } from 'vue'
import * as echarts from 'echarts'
import { Modal } from 'bootstrap'
import Loading from '@/components/Loading.vue'
import GraphFilterPanel from '@/components/graph/GraphFilterPanel.vue'
import GraphNodeDetail from '@/components/graph/GraphNodeDetail.vue'
import GraphMiniMap from '@/components/graph/GraphMiniMap.vue'
import { graphApi, type GraphNode, type GraphLink, type GraphData, type ConceptContext } from '@/services/api/graph'
import { courseApi } from '@/services/api/course'
import { chapterApi } from '@/services/api/chapter'
import type { Course } from '@/types'

// State
const loading = ref(false)
const chartContainer = ref<HTMLElement | null>(null)
const chartInstance = shallowRef<echarts.ECharts | null>(null)
const learningPathModal = ref<HTMLElement | null>(null)
let bsModal: Modal | null = null

const courses = ref<Course[]>([])
const selectedCourseId = ref('')
const layoutMode = ref<'force' | 'circular'>('force')

const graphData = ref<GraphData>({ nodes: [], links: [] })
const selectedNode = ref<GraphNode | null>(null)
const conceptContext = ref<ConceptContext | null>(null)
const showDetail = ref(false)

const filterDifficulty = ref('')
const filterProgress = ref('')
const filterRelationType = ref('')
const searchTerm = ref('')

const highlightPathMode = ref(false)
const pathNodes = ref<string[]>([])
const highlightedPathIds = ref<string[]>([])

const learningPath = ref<any[]>([])

const contextMenu = ref<{ visible: boolean; x: number; y: number; nodeId: string | null }>({
  visible: false, x: 0, y: 0, nodeId: null
})

// Computed
const statsText = computed(() => {
  const s = graphData.value.statistics
  if (!s) return '选择课程以浏览知识图谱'
  return `${s.totalNodes} 个知识点 · ${s.totalLinks} 条关系 · ${s.completedNodes} 已完成${s.isolatedCount > 0 ? ` · ${s.isolatedCount} 个孤立知识点` : ''}`
})

// Methods
async function loadCourses() {
  try {
    const res = await courseApi.getAllCourses()
    if (res.success && res.data) {
      courses.value = res.data
    }
  } catch (e) {
    console.error('Failed to load courses', e)
  }
}

async function loadGraph() {
  if (!selectedCourseId.value) {
    graphData.value = { nodes: [], links: [] }
    return
  }
  loading.value = true
  try {
    const res = await graphApi.getFullGraph(selectedCourseId.value)
    if (res.success && res.data) {
      graphData.value = res.data
      await nextTick()
      initChart()
    }
  } catch (e) {
    console.error('Failed to load graph', e)
  } finally {
    loading.value = false
  }
}

function initChart() {
  if (!chartContainer.value) return
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }

  const chart = echarts.init(chartContainer.value)
  chartInstance.value = chart

  renderChart(chart)

  chart.on('click', (params: any) => {
    contextMenu.value.visible = false
    if (params.dataType === 'node') {
      handleNodeClick(params.data)
    } else if (params.dataType === 'edge') {
      showDetail.value = false
    }
  })

  chart.on('dblclick', (params: any) => {
    if (params.dataType === 'node') {
      contextMenu.value.visible = false
      loadConceptDetail(params.data.id)
    }
  })

  // Right-click context menu
  chart.getZr().on('contextmenu', (params: any) => {
    params.event?.preventDefault?.()
    const point = chart.convertFromPixel({ seriesIndex: 0 }, [params.offsetX, params.offsetY])
    // Find nearby node
    let nodeId: string | null = null
    for (const n of graphData.value.nodes) {
      const nodePos = chart.convertToPixel({ seriesIndex: 0 }, [n.id])
      if (nodePos) {
        const dx = point[0] - nodePos[0]
        const dy = point[1] - nodePos[1]
        if (Math.sqrt(dx * dx + dy * dy) < 30) {
          nodeId = n.id
          break
        }
      }
    }
    if (nodeId) {
      contextMenu.value = { visible: true, x: params.event?.clientX || 0, y: params.event?.clientY || 0, nodeId }
    }
  })

  // Click elsewhere to close context menu
  document.addEventListener('click', () => {
    contextMenu.value.visible = false
  })
}

function renderChart(chart: echarts.ECharts) {
  const filteredNodes = applyFilters()
  const filteredNodeIds = new Set(filteredNodes.map(n => n.id))
  const filteredLinks = graphData.value.links.filter(
    l => filteredNodeIds.has(l.source) && filteredNodeIds.has(l.target)
  )

  const echartsNodes = filteredNodes.map(node => {
    const color = getNodeColor(node)
    const size = getNodeSize(node)
    const isHighlighted = highlightedPathIds.value.includes(node.id)
    const isPathNode = pathNodes.value.includes(node.id)

    return {
      id: node.id,
      name: node.name,
      symbolSize: isHighlighted ? size + 12 : isPathNode ? size + 6 : size,
      itemStyle: {
        color,
        borderColor: isHighlighted ? '#ffc107' : isPathNode ? '#0dcaf0' : getBorderColor(node),
        borderWidth: isHighlighted ? 4 : isPathNode ? 3 : getBorderWidth(node),
        shadowBlur: isHighlighted ? 20 : isPathNode ? 12 : 6,
        shadowColor: isHighlighted ? 'rgba(255,193,7,0.5)' : 'rgba(0,0,0,0.12)'
      },
      label: { show: node.importanceWeight >= 50 || isHighlighted, color: '#333', fontSize: 11 },
      category: node.category,
      difficultyLevel: node.difficultyLevel,
      _rawNode: node
    }
  })

  const echartsLinks = filteredLinks.map(link => {
    const isHighlighted = highlightedPathIds.value.includes(link.source) && highlightedPathIds.value.includes(link.target)
    return {
      source: link.source,
      target: link.target,
      lineStyle: {
        width: isHighlighted ? 4 : 1.5,
        curveness: 0.15,
        color: isHighlighted ? '#ffc107' : getLinkColor(link),
        opacity: isHighlighted ? 1 : 0.55
      },
      label: {
        show: isHighlighted,
        formatter: link.type,
        fontSize: 10,
        color: '#666'
      }
    }
  })

  const option: any = {
    tooltip: {
      backgroundColor: 'rgba(255, 255, 255, 0.96)',
      borderColor: '#e9ecef',
      textStyle: { color: '#333' },
      formatter: (params: any) => {
        if (params.dataType === 'node') {
          const n = params.data
          const statusMap: Record<string, string> = { 'NOT_STARTED': '未开始', 'IN_PROGRESS': '进行中', 'COMPLETED': '已完成', 'MASTERED': '已掌握' }
          return `
            <div style="font-weight:bold;margin-bottom:4px;">${n.name}</div>
            <div style="font-size:12px;color:#666;max-width:200px;">${n._rawNode?.description || '暂无描述'}</div>
            <div style="margin-top:4px;font-size:11px;">
              难度: Lv.${n.difficultyLevel} | 重要度: ${n._rawNode?.importanceWeight || '-'}/100<br/>
              状态: ${statusMap[n._rawNode?.progressStatus || 'NOT_STARTED'] || '未开始'}
              ${n._rawNode?.chapterTitle ? `<br/>章节: ${n._rawNode.chapterTitle}` : ''}
            </div>
          `
        }
        if (params.dataType === 'edge') {
          return `<div style="font-size:12px;">${params.data.label || params.value || '关联'}</div>`
        }
        return ''
      }
    },
    animationDurationUpdate: 800,
    animationEasingUpdate: 'quinticInOut',
    series: [{
      type: 'graph',
      layout: layoutMode.value,
      data: echartsNodes,
      links: echartsLinks,
      roam: true,
      draggable: true,
      label: {
        show: true,
        position: 'right',
        formatter: '{b}',
        fontSize: 11,
        fontWeight: 'bold'
      },
      edgeSymbol: ['none', 'arrow'],
      edgeSymbolSize: [6, 10],
      force: layoutMode.value === 'force' ? {
        repulsion: 600,
        edgeLength: [80, 200],
        gravity: 0.08,
        layoutAnimation: true
      } : undefined,
      circular: layoutMode.value === 'circular' ? {
        rotateLabel: true
      } : undefined,
      focusNodeAdjacency: true,
      emphasis: {
        focus: 'adjacency',
        lineStyle: { width: 4 },
        itemStyle: { shadowBlur: 16, shadowColor: 'rgba(0,0,0,0.25)' }
      }
    }]
  }

  chart.setOption(option, true)
}

function applyFilters(): GraphNode[] {
  let nodes = [...graphData.value.nodes]

  if (filterDifficulty.value) {
    nodes = nodes.filter(n => {
      const [min, max] = filterDifficulty.value.split('-').map(Number)
      if (max) return n.difficultyLevel >= min && n.difficultyLevel <= max
      return n.difficultyLevel === min
    })
  }

  if (filterProgress.value) {
    nodes = nodes.filter(n => n.progressStatus === filterProgress.value)
  }

  if (searchTerm.value) {
    const kw = searchTerm.value.toLowerCase()
    nodes = nodes.filter(n => n.name.toLowerCase().includes(kw))
  }

  return nodes
}

// Node color by difficulty
function getNodeColor(node: GraphNode): string {
  const level = node.difficultyLevel
  if (level <= 2) return '#28a745'
  if (level <= 3) return '#17a2b8'
  if (level <= 4) return '#ffc107'
  return '#dc3545'
}

// Node size by importance
function getNodeSize(node: GraphNode): number {
  const w = node.importanceWeight
  if (w >= 80) return 55
  if (w >= 60) return 40
  if (w >= 40) return 30
  return 22
}

// Border color by progress
function getBorderColor(node: GraphNode): string {
  switch (node.progressStatus) {
    case 'COMPLETED': return '#0d6efd'
    case 'MASTERED': return '#198754'
    case 'IN_PROGRESS': return '#fd7e14'
    default: return '#ced4da'
  }
}

function getBorderWidth(node: GraphNode): number {
  switch (node.progressStatus) {
    case 'COMPLETED':
    case 'MASTERED': return 3
    case 'IN_PROGRESS': return 2
    default: return 1
  }
}

// Link color by type
function getLinkColor(link: GraphLink): string {
  switch (link.type) {
    case 'PREREQUISITE': return '#0d6efd'
    case 'DEPENDS_ON': return '#6f42c1'
    case 'SIMILAR_TO': return '#20c997'
    case 'PART_OF': return '#adb5bd'
    case 'USES': return '#fd7e14'
    default: return '#adb5bd'
  }
}

// Node click
async function handleNodeClick(nodeData: any) {
  const node = graphData.value.nodes.find(n => n.id === nodeData.id)
  if (!node) return

  if (highlightPathMode.value) {
    if (pathNodes.value.includes(node.id)) {
      pathNodes.value = pathNodes.value.filter(id => id !== node.id)
    } else if (pathNodes.value.length < 2) {
      pathNodes.value.push(node.id)
    }
    if (pathNodes.value.length === 2) {
      await queryShortestPath(pathNodes.value[0], pathNodes.value[1])
    }
    renderChart(chartInstance.value!)
  } else {
    selectedNode.value = node
    showDetail.value = true
    await loadConceptDetail(node.id)
  }
}

async function loadConceptDetail(conceptId: string) {
  try {
    const res = await graphApi.getConceptDetail(conceptId)
    if (res.success && res.data) {
      conceptContext.value = res.data
    }
  } catch (e) {
    console.error('Failed to load concept detail', e)
  }
}

async function queryShortestPath(from: string, to: string) {
  try {
    const res = await graphApi.getShortestPath(from, to)
    if (res.success && res.data) {
      highlightedPathIds.value = res.data
    }
  } catch (e) {
    console.error('Failed to query shortest path', e)
  }
}

function setLayout(mode: 'force' | 'circular') {
  layoutMode.value = mode
  if (chartInstance.value) renderChart(chartInstance.value)
}

function resetView() {
  searchTerm.value = ''
  filterDifficulty.value = ''
  filterProgress.value = ''
  pathNodes.value = []
  highlightedPathIds.value = []
  highlightPathMode.value = false
  showDetail.value = false
  if (chartInstance.value) renderChart(chartInstance.value)
}

function resetAll() {
  resetView()
  if (chartInstance.value) {
    chartInstance.value.dispatchAction({ type: 'restore' })
  }
}

async function showLearningPath() {
  if (!selectedCourseId.value) return
  try {
    const res = await graphApi.getLearningPath(selectedCourseId.value)
    if (res.success && res.data) {
      learningPath.value = res.data.map((item: any) => ({
        ...item,
        _progressStatus: graphData.value.nodes.find(n => n.id === item.id)?.progressStatus || 'NOT_STARTED'
      }))
      if (!bsModal && learningPathModal.value) {
        bsModal = new Modal(learningPathModal.value)
      }
      bsModal?.show()
    }
  } catch (e) {
    console.error('Failed to load learning path', e)
  }
}

function highlightLearningPathOnGraph() {
  highlightedPathIds.value = learningPath.value.map((item: any) => item.id)
  bsModal?.hide()
  if (chartInstance.value) renderChart(chartInstance.value)
}

function navigateToConcept(conceptId: string) {
  const node = graphData.value.nodes.find(n => n.id === conceptId)
  if (node) {
    selectedNode.value = node
    loadConceptDetail(conceptId)
    // Optionally focus the graph on this node
    if (chartInstance.value) {
      chartInstance.value.dispatchAction({ type: 'highlight', seriesIndex: 0, dataIndex: -1 })
    }
  }
}

async function onProgressChange(conceptId: string, status: string) {
  const node = graphData.value.nodes.find(n => n.id === conceptId)
  if (!node || !node.chapterId) return
  try {
    const completed = status === 'COMPLETED' || status === 'MASTERED'
    await chapterApi.updateProgress(node.chapterId, { completed, elapsedMinutes: 30 })
    await loadGraph()
  } catch (e) {
    console.error('Failed to update progress', e)
  }
}

// Context menu actions
function onContextMarkMastered() {
  const nodeId = contextMenu.value.nodeId
  if (nodeId) {
    onProgressChange(nodeId, 'MASTERED')
  }
  contextMenu.value.visible = false
}

function onContextSetTarget() {
  const nodeId = contextMenu.value.nodeId
  if (nodeId && highlightPathMode.value) {
    if (!pathNodes.value.includes(nodeId)) {
      if (pathNodes.value.length < 2) {
        pathNodes.value.push(nodeId)
      }
    }
    if (pathNodes.value.length === 2) {
      queryShortestPath(pathNodes.value[0], pathNodes.value[1])
    }
    renderChart(chartInstance.value!)
  } else if (nodeId) {
    loadConceptDetail(nodeId)
    const node = graphData.value.nodes.find(n => n.id === nodeId)
    if (node) {
      selectedNode.value = node
      showDetail.value = true
    }
  }
  contextMenu.value.visible = false
}

function onContextShowPrereqs() {
  const nodeId = contextMenu.value.nodeId
  if (nodeId) {
    graphApi.getPrerequisites(nodeId).then(res => {
      if (res.success && res.data) {
        highlightedPathIds.value = [nodeId, ...res.data.map((p: any) => p.id)]
        renderChart(chartInstance.value!)
      }
    })
  }
  contextMenu.value.visible = false
}

function handleResize() {
  chartInstance.value?.resize()
}

onMounted(async () => {
  await loadCourses()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance.value?.dispose()
  bsModal?.dispose()
})
</script>

<style scoped>
.context-menu {
  min-width: 180px;
  animation: fadeIn 0.15s ease-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}
</style>

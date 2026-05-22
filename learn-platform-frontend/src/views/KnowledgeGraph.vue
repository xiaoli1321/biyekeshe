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
          <div class="d-flex flex-wrap gap-2 mt-2 small">
            <span class="legend-chip">
              <span class="legend-dot state-not-started"></span>未开始
            </span>
            <span class="legend-chip">
              <span class="legend-dot state-in-progress"></span>进行中
            </span>
            <span class="legend-chip">
              <span class="legend-dot state-completed"></span>已完成
            </span>
            <span class="legend-chip">
              <span class="legend-dot state-mastered"></span>已掌握
            </span>
          </div>
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
      v-model:searchTerm="searchTerm"
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
              <div class="graph-side-panel">
                <div v-if="graphData.links.length > 0" class="relation-legend">
                  <div class="relation-legend-title">关系图例</div>
                  <div
                    v-for="item in relationLegendItems"
                    :key="item.type"
                    class="relation-legend-item"
                  >
                    <div class="relation-legend-line">
                      <span
                        class="relation-line-sample"
                        :class="item.sampleClass"
                        :style="{ backgroundColor: item.color }"
                      ></span>
                      <span class="relation-line-arrow" :style="{ color: item.color }">→</span>
                    </div>
                    <div class="relation-legend-copy">
                      <div class="relation-legend-name">{{ item.label }}</div>
                      <div class="relation-legend-desc">{{ item.description }}</div>
                    </div>
                  </div>
                </div>

                <div v-if="chapterLegendItems.length > 0" class="chapter-legend">
                  <div class="relation-legend-title">章节分组</div>
                  <div
                    v-for="item in chapterLegendItems"
                    :key="item.key"
                    class="chapter-legend-item"
                  >
                    <span class="chapter-color-chip" :style="{ backgroundColor: item.color }"></span>
                    <div class="chapter-legend-copy">
                      <div class="chapter-legend-name">{{ item.title }}</div>
                      <div class="chapter-legend-desc">{{ item.count }} 个知识点</div>
                    </div>
                  </div>
                </div>

                <div class="path-guide-card" :class="{ active: highlightPathMode }">
                  <div class="path-guide-header">
                    <span>路径模式</span>
                    <span class="path-guide-status">{{ highlightPathMode ? '已开启' : '未开启' }}</span>
                  </div>
                  <div class="path-guide-text">
                    {{ highlightPathMode ? pathModeHint : '开启后依次点击两个知识点，即可在图中高亮它们之间的最短学习路径。' }}
                  </div>
                  <div v-if="highlightPathMode && pathNodeNames.length > 0" class="path-guide-selected">
                    已选择：{{ pathNodeNames.join(' → ') }}
                  </div>
                  <div v-if="highlightPathMode && highlightedPathIds.length > 1" class="path-guide-tip">
                    已找到路径，图中黄色高亮即为推荐通路。
                  </div>
                </div>
              </div>
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
import { ref, computed, onMounted, onUnmounted, nextTick, shallowRef, watch } from 'vue'
import * as echarts from 'echarts'
import { Modal } from 'bootstrap'
import Loading from '@/components/Loading.vue'
import GraphFilterPanel from '@/components/graph/GraphFilterPanel.vue'
import GraphNodeDetail from '@/components/graph/GraphNodeDetail.vue'
import GraphMiniMap from '@/components/graph/GraphMiniMap.vue'
import { graphApi, type GraphNode, type GraphLink, type GraphData, type ConceptContext } from '@/services/api/graph'
import { courseApi } from '@/services/api/course'
import type { Course } from '@/types'

// State
const loading = ref(false)
const chartContainer = ref<HTMLElement | null>(null)
const chartInstance = shallowRef<echarts.ECharts | null>(null)
const learningPathModal = ref<HTMLElement | null>(null)
let bsModal: Modal | null = null
let removeDocumentClickListener: (() => void) | null = null

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

const relationLegendItems = [
  { type: 'PREREQUISITE', label: '先修关系', description: '学习 A 之前，通常需要先掌握 B。', color: '#1d4ed8', sampleClass: 'is-solid is-bold' },
  { type: 'DEPENDS_ON', label: '依赖关系', description: '概念实现或理解时会依赖另一个概念。', color: '#7c3aed', sampleClass: 'is-dashed is-bold' },
  { type: 'USES', label: '使用关系', description: '概念在实践中会调用或使用另一个概念。', color: '#ea580c', sampleClass: 'is-dashed' },
  { type: 'SIMILAR_TO', label: '相似关系', description: '两个概念含义接近，适合对照学习。', color: '#0f766e', sampleClass: 'is-dotted' },
  { type: 'PART_OF', label: '包含关系', description: '概念属于更大的知识结构的一部分。', color: '#6b7280', sampleClass: 'is-solid' }
] as const

const chapterPalette = [
  '#0f766e',
  '#2563eb',
  '#c2410c',
  '#7c3aed',
  '#b45309',
  '#0f766e',
  '#be123c',
  '#1d4ed8'
]

const contextMenu = ref<{ visible: boolean; x: number; y: number; nodeId: string | null }>({
  visible: false, x: 0, y: 0, nodeId: null
})

// Computed
const statsText = computed(() => {
  const s = graphData.value.statistics
  if (!s) return '选择课程以浏览知识图谱'
  const chapterCount = Object.keys(s.chapterGroups || {}).length
  return `${chapterCount} 个章节分组 · ${s.totalNodes} 个知识点 · ${s.totalLinks} 条关系 · ${s.completedNodes} 已完成${s.isolatedCount > 0 ? ` · ${s.isolatedCount} 个孤立知识点` : ''}`
})

const pathNodeNames = computed(() =>
  pathNodes.value
    .map(id => graphData.value.nodes.find(node => node.id === id)?.name)
    .filter((name): name is string => Boolean(name))
)

const pathModeHint = computed(() => {
  if (pathNodes.value.length === 0) {
    return '请先点击第一个知识点作为起点，再点击第二个知识点作为终点。'
  }
  if (pathNodes.value.length === 1) {
    return `起点已选“${pathNodeNames.value[0]}”，请再点击一个目标知识点。`
  }
  return `已选择起点“${pathNodeNames.value[0]}”和终点“${pathNodeNames.value[1]}”。`
})

const chapterLegendItems = computed(() => {
  const chapterGroups = graphData.value.statistics?.chapterGroups
  if (chapterGroups && Object.keys(chapterGroups).length > 0) {
    return Object.entries(chapterGroups).map(([title, count]) => ({
      key: title,
      title,
      count,
      color: getChapterColor(title)
    }))
  }

  const counts = new Map<string, number>()
  graphData.value.nodes.forEach(node => {
    const title = getChapterDisplayName(node)
    counts.set(title, (counts.get(title) || 0) + 1)
  })

  return Array.from(counts.entries()).map(([title, count]) => ({
    key: title,
    title,
    count,
    color: getChapterColor(title)
  }))
})

// Methods
async function loadCourses() {
  try {
    const res = await courseApi.getCourses({ page: 0, size: 100 })
    if (res.success && res.data) {
      courses.value = res.data.content || []
    }
  } catch (e) {
    console.error('Failed to load courses', e)
  }
}

async function loadGraph(resetState = true) {
  if (!selectedCourseId.value) {
    graphData.value = { nodes: [], links: [] }
    selectedNode.value = null
    conceptContext.value = null
    showDetail.value = false
    pathNodes.value = []
    highlightedPathIds.value = []
    contextMenu.value.visible = false
    return
  }

  if (resetState) {
    selectedNode.value = null
    conceptContext.value = null
    showDetail.value = false
    pathNodes.value = []
    highlightedPathIds.value = []
  }

  contextMenu.value.visible = false
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

  chart.on('contextmenu', (params: any) => {
    params.event?.event?.preventDefault?.()
    if (params.dataType !== 'node' || !params.data?.id) {
      contextMenu.value.visible = false
      return
    }

    contextMenu.value = {
      visible: true,
      x: params.event?.event?.clientX || 0,
      y: params.event?.event?.clientY || 0,
      nodeId: params.data.id
    }
  })

  if (removeDocumentClickListener) {
    removeDocumentClickListener()
  }
  const handleDocumentClick = () => {
    contextMenu.value.visible = false
  }
  document.addEventListener('click', handleDocumentClick)
  removeDocumentClickListener = () => {
    document.removeEventListener('click', handleDocumentClick)
  }
}

function renderChart(chart: echarts.ECharts) {
  let filteredNodes = applyFilters()
  let filteredNodeIds = new Set(filteredNodes.map(n => n.id))
  let filteredLinks = graphData.value.links.filter(
    l => filteredNodeIds.has(l.source) && filteredNodeIds.has(l.target)
  )

  if (filterRelationType.value) {
    filteredLinks = filteredLinks.filter(link => link.type === filterRelationType.value)
    const relatedNodeIds = new Set<string>()
    filteredLinks.forEach(link => {
      relatedNodeIds.add(link.source)
      relatedNodeIds.add(link.target)
    })
    filteredNodes = filteredNodes.filter(node => relatedNodeIds.has(node.id))
    filteredNodeIds = new Set(filteredNodes.map(n => n.id))
    filteredLinks = filteredLinks.filter(
      l => filteredNodeIds.has(l.source) && filteredNodeIds.has(l.target)
    )
  }

  const echartsNodes = filteredNodes.map(node => {
    const color = getNodeColor(node)
    const size = getNodeSize(node)
    const isHighlighted = highlightedPathIds.value.includes(node.id)
    const isPathNode = pathNodes.value.includes(node.id)
    const progressStyle = getProgressVisual(node.progressStatus)
    const chapterTitle = getChapterDisplayName(node)

    return {
      id: node.id,
      name: node.name,
      symbol: progressStyle.symbol,
      symbolSize: isHighlighted ? size + 12 : isPathNode ? size + 6 : size,
      itemStyle: {
        color,
        borderColor: isHighlighted ? '#ffc107' : isPathNode ? '#0dcaf0' : progressStyle.borderColor,
        borderWidth: isHighlighted ? 5 : isPathNode ? 4 : progressStyle.borderWidth,
        shadowBlur: isHighlighted ? 20 : isPathNode ? 14 : progressStyle.shadowBlur,
        shadowColor: isHighlighted ? 'rgba(255,193,7,0.5)' : progressStyle.shadowColor
      },
      label: {
        show: node.importanceWeight >= 50 || isHighlighted || node.progressStatus !== 'NOT_STARTED',
        color: '#333',
        fontSize: 11,
        formatter: () => `${progressStyle.shortLabel}${node.name}`
      },
      category: node.category,
      chapterTitle,
      difficultyLevel: node.difficultyLevel,
      _rawNode: node
    }
  })

  const echartsLinks = filteredLinks.map(link => {
    const isHighlighted = highlightedPathIds.value.includes(link.source) && highlightedPathIds.value.includes(link.target)
    const linkVisual = getLinkVisual(link)
    return {
      source: link.source,
      target: link.target,
      relationType: link.type,
      description: link.description,
      lineStyle: {
        width: isHighlighted ? linkVisual.width + 1.8 : linkVisual.width,
        curveness: linkVisual.curveness,
        type: linkVisual.lineType,
        color: isHighlighted ? '#ffc107' : linkVisual.color,
        opacity: isHighlighted ? 1 : 0.96
      },
      label: {
        show: true,
        formatter: linkVisual.label,
        fontSize: isHighlighted ? 11 : 10,
        color: isHighlighted ? '#7c5c00' : linkVisual.color,
        backgroundColor: isHighlighted ? 'rgba(255, 243, 205, 0.92)' : 'rgba(255,255,255,0.88)',
        borderColor: isHighlighted ? '#facc15' : 'rgba(0,0,0,0.06)',
        borderWidth: 1,
        borderRadius: 10,
        padding: [3, 8]
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
            <div style="font-size:12px;color:#666;max-width:220px;">${n._rawNode?.summary || n._rawNode?.description || '暂无描述'}</div>
            <div style="margin-top:4px;font-size:11px;">
              难度: Lv.${n.difficultyLevel} | 重要度: ${n._rawNode?.importanceWeight || '-'}/100<br/>
              状态: ${statusMap[n._rawNode?.progressStatus || 'NOT_STARTED'] || '未开始'}
              ${n._rawNode?.chapterTitle ? `<br/>章节分组: ${n._rawNode.chapterTitle}` : ''}
            </div>
          `
        }
        if (params.dataType === 'edge') {
          const relationType = getRelationTypeLabel(params.data?.relationType || '关联')
          const description = params.data?.description ? `<br/>${params.data.description}` : ''
          return `<div style="font-size:12px;">${relationType}${description}</div>`
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
      edgeSymbolSize: [10, 18],
      force: layoutMode.value === 'force' ? {
        repulsion: 980,
        edgeLength: [220, 360],
        gravity: 0.03,
        friction: 0.22,
        layoutAnimation: true
      } : undefined,
      circular: layoutMode.value === 'circular' ? {
        rotateLabel: true
      } : undefined,
      focusNodeAdjacency: true,
      emphasis: {
        focus: 'adjacency',
        lineStyle: { width: 6 },
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

function getChapterDisplayName(node: GraphNode): string {
  return node.chapterTitle || '未归属章节'
}

function getChapterColor(chapterKey: string): string {
  const normalized = chapterKey || '未归属章节'
  const hash = normalized.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0)
  return chapterPalette[hash % chapterPalette.length]
}

// Node color by chapter group
function getNodeColor(node: GraphNode): string {
  return getChapterColor(getChapterDisplayName(node))
}

// Node size by importance
function getNodeSize(node: GraphNode): number {
  const w = node.importanceWeight
  if (w >= 80) return 55
  if (w >= 60) return 40
  if (w >= 40) return 30
  return 22
}

function getProgressVisual(status?: string) {
  switch (status) {
    case 'MASTERED':
      return {
        borderColor: '#198754',
        borderWidth: 5,
        shadowBlur: 18,
        shadowColor: 'rgba(25,135,84,0.28)',
        symbol: 'pin',
        shortLabel: '掌·'
      }
    case 'COMPLETED':
      return {
        borderColor: '#0d6efd',
        borderWidth: 4,
        shadowBlur: 14,
        shadowColor: 'rgba(13,110,253,0.24)',
        symbol: 'diamond',
        shortLabel: '完·'
      }
    case 'IN_PROGRESS':
      return {
        borderColor: '#fd7e14',
        borderWidth: 4,
        shadowBlur: 14,
        shadowColor: 'rgba(253,126,20,0.24)',
        symbol: 'roundRect',
        shortLabel: '学·'
      }
    default:
      return {
        borderColor: '#ced4da',
        borderWidth: 2,
        shadowBlur: 8,
        shadowColor: 'rgba(108,117,125,0.12)',
        symbol: 'circle',
        shortLabel: ''
      }
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

function getRelationTypeLabel(type: string): string {
  const relation = relationLegendItems.find(item => item.type === type)
  return relation?.label || type
}

function getLinkVisual(link: GraphLink) {
  switch (link.type) {
    case 'PREREQUISITE':
      return {
        color: '#1d4ed8',
        width: 4.8,
        lineType: 'solid' as const,
        curveness: 0.1,
        label: '先修',
        alwaysShowLabel: true
      }
    case 'DEPENDS_ON':
      return {
        color: '#7c3aed',
        width: 4,
        lineType: 'dashed' as const,
        curveness: 0.18,
        label: '依赖',
        alwaysShowLabel: true
      }
    case 'USES':
      return {
        color: '#ea580c',
        width: 3.6,
        lineType: 'dashed' as const,
        curveness: 0.24,
        label: '使用',
        alwaysShowLabel: true
      }
    case 'SIMILAR_TO':
      return {
        color: '#0f766e',
        width: 3.2,
        lineType: 'dotted' as const,
        curveness: 0.3,
        label: '相似',
        alwaysShowLabel: true
      }
    case 'PART_OF':
      return {
        color: '#6b7280',
        width: 3.4,
        lineType: 'solid' as const,
        curveness: 0.16,
        label: '包含',
        alwaysShowLabel: true
      }
    default:
      return {
        color: getLinkColor(link),
        width: 2.4,
        lineType: 'solid' as const,
        curveness: 0.15,
        label: link.type,
        alwaysShowLabel: false
      }
  }
}

// Node click
async function handleNodeClick(nodeData: any) {
  const node = graphData.value.nodes.find(n => n.id === nodeData.id)
  if (!node) return

  if (highlightPathMode.value) {
    if (pathNodes.value.length >= 2 && !pathNodes.value.includes(node.id)) {
      pathNodes.value = [node.id]
      highlightedPathIds.value = []
    } else if (pathNodes.value.includes(node.id)) {
      pathNodes.value = pathNodes.value.filter(id => id !== node.id)
      highlightedPathIds.value = []
    } else if (pathNodes.value.length < 2) {
      pathNodes.value.push(node.id)
    }
    if (pathNodes.value.length === 2) {
      await queryShortestPath(pathNodes.value[0], pathNodes.value[1])
    }
    if (chartInstance.value) renderChart(chartInstance.value)
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
    } else {
      highlightedPathIds.value = []
    }
  } catch (e) {
    console.error('Failed to query shortest path', e)
    highlightedPathIds.value = []
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
  filterRelationType.value = ''
  pathNodes.value = []
  highlightedPathIds.value = []
  highlightPathMode.value = false
  showDetail.value = false
  contextMenu.value.visible = false
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
    showDetail.value = true
    loadConceptDetail(conceptId)
  }
}

async function onProgressChange(conceptId: string, status: string) {
  try {
    await graphApi.updateConceptProgress(conceptId, { status: status as any, elapsedMinutes: 30 })
    await loadGraph(false)
    selectedNode.value = graphData.value.nodes.find(n => n.id === conceptId) || null
    if (selectedNode.value) {
      showDetail.value = true
      await loadConceptDetail(conceptId)
    }
  } catch (e) {
    console.error('Failed to update progress', e)
  }
}

// Context menu actions
function onContextMarkMastered() {
  const nodeId = contextMenu.value.nodeId
  if (nodeId) {
    void onProgressChange(nodeId, 'MASTERED')
  }
  contextMenu.value.visible = false
}

function onContextSetTarget() {
  const nodeId = contextMenu.value.nodeId
  if (nodeId && highlightPathMode.value) {
    if (pathNodes.value.length >= 2 && !pathNodes.value.includes(nodeId)) {
      pathNodes.value = [nodeId]
      highlightedPathIds.value = []
    } else if (!pathNodes.value.includes(nodeId) && pathNodes.value.length < 2) {
      pathNodes.value.push(nodeId)
    } else if (pathNodes.value.includes(nodeId) && pathNodes.value.length === 1) {
      pathNodes.value = []
      highlightedPathIds.value = []
    }
    if (pathNodes.value.length === 2) {
      void queryShortestPath(pathNodes.value[0], pathNodes.value[1])
    }
    if (chartInstance.value) renderChart(chartInstance.value)
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
        if (chartInstance.value) renderChart(chartInstance.value)
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

watch([filterDifficulty, filterProgress, filterRelationType, searchTerm], () => {
  if (chartInstance.value) {
    renderChart(chartInstance.value)
  }
})

watch(highlightPathMode, (enabled) => {
  if (!enabled) {
    pathNodes.value = []
    highlightedPathIds.value = []
  }
  if (chartInstance.value) {
    renderChart(chartInstance.value)
  }
})

watch([pathNodes, highlightedPathIds], () => {
  if (chartInstance.value) {
    renderChart(chartInstance.value)
  }
}, { deep: true })

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance.value?.dispose()
  bsModal?.dispose()
  removeDocumentClickListener?.()
})
</script>

<style scoped>
.context-menu {
  min-width: 180px;
  animation: fadeIn 0.15s ease-out;
}

.graph-side-panel {
  position: absolute;
  top: 18px;
  left: 18px;
  z-index: 11;
  width: 280px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.relation-legend,
.chapter-legend,
.path-guide-card {
  padding: 0.85rem 0.9rem;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(148, 163, 184, 0.28);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.1);
  backdrop-filter: blur(10px);
}

.relation-legend-title {
  font-size: 0.92rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 0.65rem;
}

.relation-legend-item {
  display: flex;
  align-items: flex-start;
  gap: 0.7rem;
}

.relation-legend-item + .relation-legend-item {
  margin-top: 0.7rem;
  padding-top: 0.7rem;
  border-top: 1px solid rgba(226, 232, 240, 0.9);
}

.chapter-legend-item {
  display: flex;
  align-items: center;
  gap: 0.7rem;
}

.chapter-legend-item + .chapter-legend-item {
  margin-top: 0.65rem;
  padding-top: 0.65rem;
  border-top: 1px solid rgba(226, 232, 240, 0.9);
}

.relation-legend-line {
  display: flex;
  align-items: center;
  min-width: 66px;
  padding-top: 0.2rem;
}

.relation-line-sample {
  display: inline-block;
  width: 44px;
  height: 0;
  border-top-width: 3px;
  border-top-style: solid;
}

.relation-line-sample.is-bold {
  border-top-width: 4px;
}

.relation-line-sample.is-dashed {
  border-top-style: dashed;
}

.relation-line-sample.is-dotted {
  border-top-style: dotted;
}

.relation-line-arrow {
  margin-left: 0.3rem;
  font-size: 0.95rem;
  line-height: 1;
  font-weight: 700;
}

.relation-legend-copy {
  min-width: 0;
}

.chapter-color-chip {
  width: 0.95rem;
  height: 0.95rem;
  border-radius: 999px;
  box-shadow: 0 0 0 4px rgba(15, 23, 42, 0.06);
  flex-shrink: 0;
}

.chapter-legend-copy {
  min-width: 0;
}

.chapter-legend-name {
  font-size: 0.84rem;
  font-weight: 700;
  color: #1f2937;
}

.chapter-legend-desc {
  margin-top: 0.12rem;
  font-size: 0.74rem;
  color: #64748b;
}

.relation-legend-name {
  font-size: 0.86rem;
  font-weight: 700;
  color: #1f2937;
}

.relation-legend-desc {
  margin-top: 0.18rem;
  font-size: 0.74rem;
  line-height: 1.35;
  color: #64748b;
}

.path-guide-card.active {
  border-color: rgba(234, 88, 12, 0.45);
  box-shadow: 0 14px 30px rgba(234, 88, 12, 0.12);
}

.path-guide-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 0.9rem;
  font-weight: 700;
  color: #0f172a;
}

.path-guide-status {
  padding: 0.15rem 0.5rem;
  border-radius: 999px;
  font-size: 0.72rem;
  background: #eef2ff;
  color: #4338ca;
}

.path-guide-card.active .path-guide-status {
  background: #fff7ed;
  color: #c2410c;
}

.path-guide-text {
  margin-top: 0.55rem;
  font-size: 0.78rem;
  line-height: 1.45;
  color: #475569;
}

.path-guide-selected,
.path-guide-tip {
  margin-top: 0.55rem;
  padding: 0.5rem 0.6rem;
  border-radius: 12px;
  font-size: 0.75rem;
}

.path-guide-selected {
  background: #eff6ff;
  color: #1d4ed8;
}

.path-guide-tip {
  background: #fff7ed;
  color: #c2410c;
}

.legend-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.15rem 0.55rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid #e9ecef;
}

.legend-dot {
  width: 0.7rem;
  height: 0.7rem;
  border-radius: 999px;
  display: inline-block;
}

.state-not-started {
  background: #ced4da;
}

.state-in-progress {
  background: #fd7e14;
}

.state-completed {
  background: #0d6efd;
}

.state-mastered {
  background: #198754;
}

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

@media (max-width: 768px) {
  .graph-side-panel {
    width: calc(100% - 36px);
    max-width: 320px;
  }
}
</style>

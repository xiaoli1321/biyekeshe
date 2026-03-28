<template>
  <div class="explore-page-wrapper">
    <div class="explore-container">
      <!-- Title Section -->
      <div class="explore-header-section mb-4">
        <h1 class="explore-title mb-2">探索 <span class="text-primary-alt">AgentX</span> 的应用</h1>
        <p class="explore-subtitle text-secondary">使用这些模板应用程序，或根据模板自定义您自己的应用程序。</p>
      </div>

      <!-- Integrated Navigation Bar (Tabs + Search) -->
      <div class="explore-nav-bar bg-light-gray rounded-3 p-1 d-flex justify-content-between align-items-center mb-4">
        <div class="tabs-pills-container d-flex gap-1 overflow-auto px-1">
          <button 
            v-for="tab in tabs" 
            :key="tab.id"
            :class="['tab-pill', { active: activeTab === tab.id }]"
            @click="activeTab = tab.id"
          >
            <i v-if="tab.icon" :class="['bi', tab.icon, 'me-1']"></i>
            {{ tab.name }}
          </button>
        </div>
        <div class="search-input-box pe-2 ms-3">
          <div class="input-group input-group-sm border rounded-2 bg-white search-pill">
            <span class="input-group-text bg-transparent border-0 text-muted"><i class="bi bi-search"></i></span>
            <input 
              type="text" 
              class="form-control border-0 px-1" 
              placeholder="搜索..." 
              v-model="searchQuery"
            />
          </div>
        </div>
      </div>

      <!-- Main Content Area -->
      <div class="explore-content-viewport border rounded-3 bg-white-pure min-vh-50 d-flex flex-column">
        <div v-if="loading" class="text-center py-5 my-auto">
          <div class="spinner-border text-primary" role="status"></div>
        </div>

        <div v-else-if="filteredAgents.length === 0" class="empty-state-view my-auto py-5 text-center">
          <div class="empty-hero-icon mb-4">
            <i class="bi bi-search display-1 text-light-gray"></i>
          </div>
          <h3 class="h4 fw-bold mb-2">暂无{{ activeTabName }}类型的助理</h3>
          <p class="text-muted">敬请期待更多内容</p>
        </div>

        <div v-else class="explore-grid p-4 row g-4">
          <div v-for="agent in filteredAgents" :key="agent.id" class="col-sm-6 col-md-4 col-xl-3">
            <div class="agent-compact-card h-100 border rounded-3 p-4 transition-all" @click="goToChat(agent.id)">
               <div class="agent-avatar-sq bg-light-gray rounded-3 mb-3 d-flex align-items-center justify-content-center">
                  <i class="bi bi-robot text-primary h3 mb-0"></i>
               </div>
               <div class="agent-body mb-4">
                  <h5 class="fw-bold text-dark mb-2 text-truncate">{{ agent.name }}</h5>
                  <p class="text-muted small line-clamp-2 mb-0">{{ agent.description || '体验智能助手的卓越能力' }}</p>
               </div>
               <div class="agent-footer-alt d-flex justify-content-between align-items-center">
                  <span class="badge bg-light text-secondary border px-2 py-1">DeepSeek</span>
                  <button class="btn btn-sm btn-outline-primary px-3 rounded-pill">开始对话</button>
               </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { agentApi, Agent } from '@/services/api/agent'

const router = useRouter()
const loading = ref(true)
const agents = ref<Agent[]>([])
const searchQuery = ref('')
const activeTab = ref('recommend')

const tabs = [
  { id: 'recommend', name: '推荐', icon: 'bi-stars' },
  { id: 'agent', name: 'Agent' },
  { id: 'assistant', name: '助手' },
  { id: 'deepseek', name: 'DeepSeek' },
  { id: 'media', name: '媒体' },
  { id: 'workflow', name: '工作流' },
  { id: 'writing', name: '写作' }
]

const activeTabName = computed(() => {
  return tabs.find(t => t.id === activeTab.value)?.name || ''
})

const filteredAgents = computed(() => {
  let result = agents.value || []
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(a => 
      a.name.toLowerCase().includes(q) ||
      (a.description && a.description.toLowerCase().includes(q))
    )
  }
  // Mock categorization - Only DeepSeek for demo if selected, otherwise Recommend/Agent
  if (activeTab.value === 'deepseek') {
    return result.filter(a => a.modelId.toLowerCase().includes('deepseek'))
  }
  if (activeTab.value !== 'recommend' && activeTab.value !== 'agent') {
    return [] 
  }
  return result
})

const fetchAgents = async () => {
  try {
    loading.value = true
    const response = await agentApi.getAgents()
    agents.value = response || []
  } catch (err) {
    console.error('Failed to fetch agents', err)
    agents.value = []
  } finally {
    loading.value = false
  }
}

const goToChat = (agentId: string) => {
  router.push(`/chat/${agentId}`)
}

onMounted(fetchAgents)
</script>

<style scoped>
.explore-page-wrapper {
  background-color: #fafbfc;
  min-height: 100vh;
  padding: 3rem 2rem;
}

.explore-container {
  max-width: 1300px;
  margin: 0 auto;
}

.explore-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1a1a1a;
}

.text-primary-alt {
  color: #0d6efd;
}

.explore-subtitle {
  font-size: 0.95rem;
  opacity: 0.8;
}

.explore-nav-bar {
  background-color: #f3f4f6;
}

.tab-pill {
  background: transparent;
  border: none;
  padding: 6px 16px;
  font-size: 0.9rem;
  font-weight: 500;
  color: #666;
  border-radius: 6px;
  transition: all 0.2s;
  white-space: nowrap;
}

.tab-pill:hover {
  color: #0d6efd;
}

.tab-pill.active {
  background-color: #fff;
  color: #0d6efd;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.search-pill {
  width: 240px;
  box-shadow: none !important;
}

.bg-white-pure {
  background-color: #fff;
}

.min-vh-50 {
  min-height: 500px;
}

.empty-hero-icon {
  opacity: 0.6;
}

.text-light-gray {
  color: #e5e7eb;
}

.agent-compact-card {
  background: #fff;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
}

.agent-compact-card:hover {
  transform: translateY(-4px);
  border-color: #0d6efd !important;
  box-shadow: 0 10px 25px rgba(0,0,0,0.05);
}

.agent-avatar-sq {
  width: 54px;
  height: 54px;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 2.8rem;
}

.x-small {
  font-size: 0.75rem;
}

.transition-all {
  transition: all 0.2s ease-in-out;
}
</style>

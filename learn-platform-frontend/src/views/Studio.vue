<template>
  <div class="studio-container">
    <!-- Top Search Bar -->
    <div class="search-section mb-5">
      <div class="search-input-wrapper mx-auto">
        <i class="bi bi-search search-icon"></i>
        <input type="text" v-model="searchQuery" class="form-control search-field" placeholder="搜索助理...">
      </div>
    </div>

    <div class="studio-header d-flex justify-content-between align-items-end mb-4 px-2">
      <div>
        <h1 class="h3 fw-bold mb-1">我的智能体</h1>
        <p class="text-muted small mb-0">管理您的自定义 AI 助理</p>
      </div>
      <button class="btn btn-primary px-3 shadow-sm" @click="createNewAgent">
        <i class="bi bi-plus-lg me-2"></i>创建智能体
      </button>
    </div>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
    </div>

    <div v-else-if="!loading && filteredAgents.length === 0" class="empty-state text-center py-5">
      <div class="empty-icon-box mb-3">
         <i class="bi bi-robot"></i>
      </div>
      <h3 class="h5 fw-bold">找不到相关的智能体</h3>
      <p class="text-muted small">试着换一个关键词，或创建一个新的智能助理。</p>
    </div>

    <div v-else class="row g-4 px-2">
      <div v-for="agent in filteredAgents" :key="agent.id" class="col-md-6 col-lg-4">
        <div class="agent-sleek-card h-100 shadow-sm border clickable-card" @click="goToChat(agent.id)">
          <div class="card-body p-3">
            <div class="d-flex justify-content-between align-items-start mb-3">
              <div class="d-flex align-items-center gap-3">
                <div class="agent-square-icon bg-primary shadow-sm">
                  <i v-if="!agent.avatar" class="bi bi-robot text-white"></i>
                  <img v-else :src="agent.avatar" class="avatar-img-square" />
                </div>
                <div>
                  <h6 class="fw-bold mb-0 text-dark">{{ agent.name }}</h6>
                  <div class="text-muted x-small mt-1 d-flex align-items-center gap-1">
                    <span>AI 助理</span>
                    <span class="dot">·</span>
                    <span :class="agent.enabled !== false ? 'text-success' : 'text-danger'">
                      {{ agent.enabled !== false ? '已启用' : '已禁用' }}
                    </span>
                    <span class="dot">·</span>
                    <span>更新于 {{ formatDateShort(agent.lastModifiedAt || agent.createdAt) }}</span>
                  </div>
                </div>
              </div>
              <div class="dropdown-container" @click.stop>
                <button class="btn-action-trigger" @click="toggleDropdown(agent.id)">
                  <i class="bi bi-three-dots"></i>
                </button>
                <div v-if="activeDropdownId === agent.id" class="custom-dropdown-menu shadow-lg border animate-fade-in">
                  <div class="dropdown-header-sm px-3 py-1 text-muted x-small fw-bold border-bottom">操作</div>
                  <button class="dropdown-item-custom mt-1" @click="editAgent(agent.id)">
                    <i class="bi bi-pencil-square me-2"></i>编辑
                  </button>
                  <hr class="mx-2 my-1 opacity-10">
                  <button class="dropdown-item-custom text-danger mb-1" @click="confirmDelete(agent)">
                    <i class="bi bi-trash3 me-2"></i>删除
                  </button>
                </div>
              </div>
            </div>
            <p class="card-text text-secondary small line-clamp-2 mt-2 mb-0">
              {{ agent.description || '还没有为这个助理添加描述...' }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Delete Confirmation Modal (Improved) -->
    <div v-if="showDeleteModal" class="modal-overlay-custom" @click.self="showDeleteModal = false">
      <div class="modal-card-small shadow-lg animate-fade-in">
        <div class="p-4">
          <h5 class="fw-bold mb-3">确定删除吗？</h5>
          <p class="text-muted small">删除后将无法恢复智能体 <b>{{ agentToDelete?.name }}</b> 的所有配置。此操作无法撤销。</p>
          <div class="d-flex justify-content-end gap-2 mt-4">
            <button class="btn btn-light btn-sm border px-3" @click="showDeleteModal = false">取消</button>
            <button class="btn btn-danger btn-sm px-3" @click="handleDelete">确认删除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { agentApi, Agent } from '@/services/api/agent'
import { useAgentStore } from '@/stores/agent'

const router = useRouter()
const agentStore = useAgentStore()
const searchQuery = ref('')
const activeDropdownId = ref<string | null>(null)
const showDeleteModal = ref(false)
const agentToDelete = ref<Agent | null>(null)

const agents = computed(() => agentStore.myAgents)
const loading = computed(() => agentStore.loading)

const filteredAgents = computed(() => {
  if (!agents.value) return []
  if (!searchQuery.value) return agents.value
  const q = searchQuery.value.toLowerCase()
  return agents.value.filter(a => 
    a.name.toLowerCase().includes(q) || 
    (a.description && a.description.toLowerCase().includes(q))
  )
})

const toggleDropdown = (id: string) => {
  activeDropdownId.value = activeDropdownId.value === id ? null : id
}

const closeDropdown = () => {
  activeDropdownId.value = null
}

const formatDateShort = (date: string) => {
  if (!date) return '未知'
  const d = new Date(date)
  return `${d.getFullYear()}/${d.getMonth() + 1}/${d.getDate()}`
}

const createNewAgent = () => router.push('/studio/new')
const editAgent = (id: string) => router.push(`/studio/${id}`)
const goToChat = (id: string) => router.push(`/chat/${id}`)

const confirmDelete = (agent: Agent) => {
  agentToDelete.value = agent
  showDeleteModal.value = true
}

const handleDelete = async () => {
  if (!agentToDelete.value) return
  try {
    await agentApi.deleteAgent(agentToDelete.value.id)
    await agentStore.fetchMyAgents() // Refresh store
    showDeleteModal.value = false
  } catch (error) {
    console.error('Delete failed', error)
  }
}

onMounted(() => {
  agentStore.fetchMyAgents()
  window.addEventListener('click', closeDropdown)
})

onUnmounted(() => {
  window.removeEventListener('click', closeDropdown)
})
</script>

<style scoped>
.studio-container {
  padding: 3rem 2rem;
  max-width: 1200px;
  margin: 0 auto;
  min-height: 100vh;
}

.search-input-wrapper {
  max-width: 800px;
  position: relative;
}

.search-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
}

.search-field {
  padding-left: 45px;
  height: 50px;
  border-radius: 10px;
  border: 1px solid #eee;
  box-shadow: 0 2px 10px rgba(0,0,0,0.02);
  transition: all 0.2s;
}

.search-field:focus {
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  border-color: #0d6efd;
}

.agent-sleek-card {
  border-radius: 12px;
  background: #fff;
  transition: all 0.2s;
  cursor: default;
}

.agent-sleek-card:hover {
  transform: translateY(-3px);
  border-color: #0d6efd99 !important;
  box-shadow: 0 8px 25px rgba(0,0,0,0.06) !important;
}

.clickable-card {
  cursor: pointer;
}

.agent-square-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.4rem;
  flex-shrink: 0;
  overflow: hidden;
}

.avatar-img-square {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.x-small {
  font-size: 0.75rem;
}

.dot {
  margin: 0 2px;
  color: #ccc;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 2.8rem;
}

.dropdown-item {
  font-size: 0.95rem;
  font-weight: 500;
  color: #333;
  padding-left: 1.25rem;
  padding-right: 1.25rem;
}

.custom-dropdown {
  min-width: 180px;
  border-radius: 12px;
}

.custom-dropdown .dropdown-item:hover {
  background-color: #f8f9fa;
  color: #0d6efd;
}

.custom-dropdown .dropdown-item.text-danger:hover {
  background-color: #fff5f5;
  color: #dc3545 !important;
}

.fs-5 { font-size: 1.2rem !important; }

.empty-icon-box {
  width: 80px;
  height: 80px;
  background: #f8f9fa;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.5rem;
  color: #ccc;
  margin: 0 auto;
}

/* Modal Overlay */
.modal-overlay-custom {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.3);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2100;
}

.modal-card-small {
  width: 100%;
  max-width: 450px;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
}

.animate-fade-in {
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}
.dropdown-container {
  position: relative;
}

.btn-action-trigger {
  background: transparent;
  border: none;
  color: #adb5bd;
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.2s;
}

.btn-action-trigger:hover {
  background: #f1f3f5;
  color: #495057;
}

.custom-dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  z-index: 1000;
  min-width: 160px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  margin-top: 8px;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.08);
}

.dropdown-item-custom {
  width: 100%;
  text-align: left;
  padding: 10px 16px;
  background: transparent;
  border: none;
  font-size: 0.875rem;
  color: #495057;
  display: flex;
  align-items: center;
  transition: background 0.2s;
}

.dropdown-item-custom:hover {
  background: #f8f9fa;
}

.dropdown-item-custom.text-danger:hover {
  background: #fff5f5;
}

.animate-fade-in {
  animation: fadeIn 0.15s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>

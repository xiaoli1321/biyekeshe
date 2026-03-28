<template>
  <aside class="app-sidebar border-end bg-white">
    <div class="sidebar-header p-3 mb-2">
      <div class="logo-box d-flex align-items-center gap-2">
         <div class="logo-circle bg-primary-subtle text-primary">
            <i class="bi bi-compass"></i>
         </div>
         <span class="fw-bold text-dark">Agent平台</span>
      </div>
    </div>

    <nav class="sidebar-nav px-2 flex-grow-1 overflow-auto">
      <div class="nav-section mb-1">
        <router-link to="/explore" class="sidebar-item rounded-2" :class="{ active: $route.path === '/explore' }">
          <i class="bi bi-compass-fill me-3"></i>
          <span>探索</span>
        </router-link>
      </div>

      <div class="nav-section mb-1">
        <div class="sidebar-header-sm px-3 py-2 d-flex justify-content-between align-items-center mb-1 clickable" @click="toggleWorkspace">
           <div class="d-flex align-items-center">
              <i class="bi bi-folder2-open me-3 text-muted"></i>
              <span class="small fw-bold text-secondary">工作区</span>
           </div>
           <i class="bi bi-chevron-down x-small text-muted transition-all" :class="{ 'rotate-180': workspaceOpen }"></i>
        </div>
        
        <div v-show="workspaceOpen" class="workspace-items animate-fade-in">
           <router-link to="/studio" class="sidebar-item rounded-2 ms-2 mb-1" :class="{ active: $route.path === '/studio' }">
             <i class="bi bi-layout-text-window-reverse me-3"></i>
             <span>我的工作室</span>
           </router-link>
           
           <!-- Dynamic Favorite Agents (Mock or Real) -->
           <div v-for="agent in favoriteAgents" :key="agent.id">
             <router-link :to="'/chat/' + agent.id" class="sidebar-item rounded-2 ms-2 mb-1" :class="{ active: $route.path.includes(agent.id) }">
                <i class="bi bi-robot me-3 text-primary-alt"></i>
                <span class="text-truncate" style="max-width: 120px;">{{ agent.name }}</span>
             </router-link>
           </div>
        </div>
      </div>
    </nav>

    <div class="sidebar-footer p-3 border-top">
       <div class="user-pill d-flex align-items-center gap-2 p-2 rounded-3 hover-bg">
          <div class="user-avatar-sm bg-secondary text-white rounded-circle d-flex align-items-center justify-content-center">
             <i class="bi bi-person"></i>
          </div>
          <div class="user-info overflow-hidden">
             <div class="small fw-bold text-truncate">{{ authStore.currentUser?.username || '用户' }}</div>
          </div>
       </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useAgentStore } from '@/stores/agent'

const authStore = useAuthStore()
const agentStore = useAgentStore()
const workspaceOpen = ref(true)

// Take top 5 agents as workspace favorites
const favoriteAgents = computed(() => agentStore.myAgents.slice(0, 5))

const toggleWorkspace = () => {
  workspaceOpen.value = !workspaceOpen.value
}

onMounted(() => {
  agentStore.fetchMyAgents()
})
</script>

<style scoped>
.app-sidebar {
  width: 260px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.logo-circle {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

.sidebar-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  text-decoration: none;
  color: #555;
  font-size: 0.95rem;
  font-weight: 500;
  transition: all 0.2s;
}

.sidebar-item:hover {
  background-color: #f3f4f6;
  color: #0d6efd;
}

.sidebar-item.active {
  background-color: #eef5ff;
  color: #0d6efd;
}

.sidebar-header-sm {
  cursor: pointer;
  transition: background 0.2s;
}

.sidebar-header-sm:hover {
  background: #f9fafb;
}

.text-primary-alt {
  color: #0d6efd;
}

.x-small { font-size: 0.7rem; }
.transition-all { transition: all 0.3s; }
.rotate-180 { transform: rotate(180deg); }

.hover-bg:hover {
  background: #f8f9fa;
  cursor: pointer;
}

.user-avatar-sm {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
}

.animate-fade-in {
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; height: 0; }
  to { opacity: 1; height: auto; }
}
</style>

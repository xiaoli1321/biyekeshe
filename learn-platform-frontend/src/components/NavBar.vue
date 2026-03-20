<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container">
      <router-link class="navbar-brand" to="/dashboard">
        <i class="bi bi-book-half me-2"></i>
        学习平台
      </router-link>

      <button
        class="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#navbarNav"
      >
        <span class="navbar-toggler-icon"></span>
      </button>

      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav me-auto">
          <li class="nav-item">
            <router-link class="nav-link" to="/dashboard">
              <i class="bi bi-speedometer2 me-1"></i>
              仪表盘
            </router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" to="/courses">
              <i class="bi bi-collection me-1"></i>
              课程
            </router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" to="/finance">
              <i class="bi bi-grid me-1"></i>
              金融中心
            </router-link>
          </li>
          <li v-if="authStore.isAdmin" class="nav-item">
            <router-link class="nav-link" to="/admin/courses">
              <i class="bi bi-gear me-1"></i>
              课程管理
            </router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" to="/graph">
              <i class="bi bi-diagram-3 me-1"></i>
              知识图谱
            </router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" to="/workflow">
              <i class="bi bi-lightning-charge me-1"></i>
              AI 工作流
            </router-link>
          </li>
        </ul>

        <ul class="navbar-nav">
          <li class="nav-item dropdown">
            <a
              class="nav-link dropdown-toggle"
              href="#"
              id="navbarDropdown"
              role="button"
              data-bs-toggle="dropdown"
            >
              <i class="bi bi-person-circle me-1"></i>
              {{ authStore.currentUser?.username || '用户' }}
            </a>
            <ul class="dropdown-menu dropdown-menu-end">
              <li>
                <a class="dropdown-item" href="#" @click="handleLogout">
                  <i class="bi bi-box-arrow-right me-2"></i>
                  退出登录
                </a>
              </li>
            </ul>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>

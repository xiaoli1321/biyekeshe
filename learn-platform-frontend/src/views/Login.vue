<template>
  <div class="auth-page">
    <div class="auth-content">
      <div class="auth-hero">
        <div class="auth-badge">LC</div>
        <h1 class="auth-title">Learn</h1>
        <p class="auth-subtitle">
          基于知识图谱的编程学习平台，连接知识点与学习路径，帮助你高效掌握核心技能。
        </p>
      </div>

      <div class="auth-card">
        <h2 class="form-title">
          <i class="bi bi-box-arrow-in-right me-2"></i>
          用户登录
        </h2>

        <div v-if="errorMessage" class="alert alert-danger" role="alert">
          <i class="bi bi-exclamation-triangle me-2"></i>
          {{ errorMessage }}
        </div>

        <form @submit.prevent="handleLogin">
          <div class="mb-3">
            <label for="email" class="form-label">
              <i class="bi bi-envelope me-1"></i>
              邮箱
            </label>
            <input
              type="email"
              class="form-control"
              id="email"
              v-model="form.email"
              required
              :disabled="loading"
            />
          </div>

          <div class="mb-3">
            <label for="password" class="form-label">
              <i class="bi bi-lock me-1"></i>
              密码
            </label>
            <input
              type="password"
              class="form-control"
              id="password"
              v-model="form.password"
              required
              :disabled="loading"
            />
          </div>

          <button
            type="submit"
            class="btn btn-primary w-100"
            :disabled="loading"
          >
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            <i v-else class="bi bi-box-arrow-in-right me-2"></i>
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>

        <div class="text-center mt-3">
          <p class="mb-0 text-muted">
            还没有账号？
            <router-link to="/register">立即注册</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const errorMessage = ref('')

const form = reactive({
  email: '',
  password: ''
})

const handleLogin = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    const result = await authStore.login(form.email, form.password)
    if (result?.success) {
      router.push('/dashboard')
      return
    }
    errorMessage.value = result?.message || '登录失败，请检查邮箱和密码'
  } catch (error: any) {
    errorMessage.value = error?.message || '登录失败，请检查邮箱和密码'
  } finally {
    loading.value = false
  }
}
</script>

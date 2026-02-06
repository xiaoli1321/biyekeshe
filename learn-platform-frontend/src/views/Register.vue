<template>
  <div class="auth-page">
    <div class="auth-content">
      <div class="auth-hero">
        <div class="auth-badge">LC</div>
        <h1 class="auth-title">Learn</h1>
        <p class="auth-subtitle">
          加入学习平台，打造你的知识图谱与学习路径，持续积累并可视化成长。
        </p>
      </div>

      <div class="auth-card">
        <h2 class="form-title">
          <i class="bi bi-person-plus me-2"></i>
          用户注册
        </h2>

        <div v-if="errorMessage" class="alert alert-danger" role="alert">
          <i class="bi bi-exclamation-triangle me-2"></i>
          {{ errorMessage }}
        </div>

        <div v-if="successMessage" class="alert alert-success" role="alert">
          <i class="bi bi-check-circle me-2"></i>
          {{ successMessage }}
        </div>

        <form @submit.prevent="handleRegister">
          <div class="mb-3">
            <label for="username" class="form-label">
              <i class="bi bi-person me-1"></i>
              用户名
            </label>
            <input
              type="text"
              class="form-control"
              id="username"
              v-model="form.username"
              required
              :disabled="loading"
            />
          </div>

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
              minlength="6"
            />
          </div>

          <div class="mb-3">
            <label for="confirmPassword" class="form-label">
              <i class="bi bi-lock-fill me-1"></i>
              确认密码
            </label>
            <input
              type="password"
              class="form-control"
              id="confirmPassword"
              v-model="form.confirmPassword"
              required
              :disabled="loading"
              minlength="6"
            />
          </div>

          <button
            type="submit"
            class="btn btn-primary w-100"
            :disabled="loading"
          >
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            <i v-else class="bi bi-person-plus me-2"></i>
            {{ loading ? '注册中...' : '注册' }}
          </button>
        </form>

        <div class="text-center mt-3">
          <p class="mb-0 text-muted">
            已有账号？
            <router-link to="/login">立即登录</router-link>
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
const successMessage = ref('')

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const handleRegister = async () => {
  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  if (form.password !== form.confirmPassword) {
    errorMessage.value = '两次输入的密码不一致'
    loading.value = false
    return
  }

  try {
    const result = await authStore.register({
      username: form.username,
      email: form.email,
      password: form.password
    })

    if (result?.success) {
      successMessage.value = result.message || '注册成功！正在跳转到登录页面...'
      setTimeout(() => {
        router.push('/login')
      }, 2000)
      return
    }

    errorMessage.value = result?.message || '注册失败，请重试'
  } catch (error: any) {
    errorMessage.value = error?.message || '注册失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

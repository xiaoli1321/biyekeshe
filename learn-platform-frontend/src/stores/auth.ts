import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, LoginCredentials, RegisterData, AuthResponse } from '@/types'
import { authApi } from '@/services/api/auth'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))
  const isLoading = ref(false)

  // 计算属性
  const isAuthenticated = computed(() => !!user.value && !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const currentUser = computed(() => user.value)

  // 初始化时从localStorage恢复用户信息
  const savedUser = localStorage.getItem('user')
  if (savedUser) {
    try {
      user.value = JSON.parse(savedUser)
    } catch (e) {
      console.error('Failed to parse saved user:', e)
      localStorage.removeItem('user')
    }
  }

  // 登录 - 支持 email/password 参数重载
  const login = async (email: string, password: string) => {
    try {
      isLoading.value = true
      const response = await authApi.login({ email, password })

      if (response.success && response.data) {
        const authResponse: AuthResponse = response.data

        // 保存token和用户信息
        token.value = authResponse.token
        user.value = authResponse.user

        // 保存到localStorage
        localStorage.setItem('token', authResponse.token)
        localStorage.setItem('user', JSON.stringify(authResponse.user))

        return { success: true }
      } else {
        return { success: false, message: response.message || '登录失败' }
      }
    } catch (error: any) {
      console.error('Login error:', error)
      const message = typeof error === 'string'
        ? error
        : (error?.message || error?.errorCode || '登录失败，请检查网络连接')
      return {
        success: false,
        message
      }
    } finally {
      isLoading.value = false
    }
  }

  // 注册
  const register = async (userData: RegisterData) => {
    try {
      isLoading.value = true
      const response = await authApi.register(userData)

      if (response.success) {
        return { success: true, message: '注册成功，请登录' }
      } else {
        return { success: false, message: response.message || '注册失败' }
      }
    } catch (error: any) {
      console.error('Register error:', error)
      return {
        success: false,
        message: error.message || '注册失败，请稍后重试'
      }
    } finally {
      isLoading.value = false
    }
  }

  // 登出
  const logout = async () => {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      // 清除本地数据
      user.value = null
      token.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }

  // 获取用户信息
  const fetchUserProfile = async () => {
    if (!token.value) return

    try {
      const response = await authApi.getProfile()
      if (response.success && response.data) {
        user.value = response.data
        localStorage.setItem('user', JSON.stringify(response.data))
      }
    } catch (error: any) {
      console.error('Failed to fetch user profile:', error)
      // 如果获取失败，清除本地数据
      logout()
    }
  }

  return {
    // 状态
    user,
    token,
    isLoading,

    // 计算属性
    isAuthenticated,
    isAdmin,
    currentUser,

    // 方法
    login,
    register,
    logout,
    fetchUserProfile
  }
})

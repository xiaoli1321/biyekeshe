import http from '../http'
import type { LoginCredentials, RegisterData, ApiResponse, User, AuthResponse } from '@/types'

export const authApi = {
  // 用户登录
  login: (credentials: LoginCredentials): Promise<ApiResponse<AuthResponse>> =>
    http.post('/auth/login', credentials),

  // 用户注册
  register: (userData: RegisterData): Promise<ApiResponse<User>> =>
    http.post('/auth/register', userData),

  // 获取当前用户信息
  getProfile: (): Promise<ApiResponse<User>> =>
    http.get('/auth/profile'),

  // 用户登出
  logout: (): Promise<ApiResponse<void>> =>
    http.post('/auth/logout')
}
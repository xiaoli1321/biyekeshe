import http from '../http'
import type { DashboardStats, LearningPath, ApiResponse, Course } from '@/types'

export const dashboardApi = {
  // 获取仪表盘统计数据
  getStats: (): Promise<ApiResponse<DashboardStats>> =>
    http.get('/dashboard/stats'),

  // 获取推荐课程
  getRecommendedCourses: (): Promise<ApiResponse<Course[]>> =>
    http.get('/dashboard/recommended-courses'),

  // 获取学习路径
  getLearningPath: (courseId: string): Promise<ApiResponse<LearningPath>> =>
    http.get(`/dashboard/learning-path/${courseId}`),

  // 获取学习建议
  getLearningSuggestions: (courseId: string): Promise<ApiResponse<string[]>> =>
    http.get(`/dashboard/learning-suggestions?courseId=${courseId}`)
}

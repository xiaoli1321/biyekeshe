import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { DashboardStats, LearningPath, Course } from '@/types'
import { dashboardApi } from '@/services/api/dashboard'
import { normalizeCourseList } from '@/utils/normalize'

export const useDashboardStore = defineStore('dashboard', () => {
  // 状态
  const stats = ref<DashboardStats | null>(null)
  const recentCourses = ref<Course[]>([])
  const learningPath = ref<LearningPath | null>(null)
  const loading = ref(false)

  // 加载仪表盘数据
  const loadDashboardData = async () => {
    try {
      loading.value = true

      // 加载统计数据
      const statsResponse = await dashboardApi.getStats()
      if (statsResponse.success && statsResponse.data) {
        const recommendedCourses = normalizeCourseList(statsResponse.data.recommendedCourses)
        stats.value = {
          totalCourses: statsResponse.data.totalCourses || 0,
          completedChapters: statsResponse.data.completedChapters || 0,
          totalHours: statsResponse.data.totalHours || 0,
          recommendedCourses
        }

        if (recommendedCourses.length > 0) {
          recentCourses.value = recommendedCourses
        }
      }

      // 加载推荐课程（作为“我的课程”展示）
      if (recentCourses.value.length === 0) {
        const coursesResponse = await dashboardApi.getRecommendedCourses()
        if (coursesResponse.success && coursesResponse.data) {
          recentCourses.value = normalizeCourseList(coursesResponse.data)
        } else {
          recentCourses.value = []
        }
      }

      // 加载学习路径
      const firstCourseId = recentCourses.value[0]?.id
      if (firstCourseId) {
        const learningPathResponse = await dashboardApi.getLearningPath(firstCourseId)
        if (learningPathResponse.success && learningPathResponse.data) {
          learningPath.value = learningPathResponse.data
        } else {
          learningPath.value = null
        }
      } else {
        learningPath.value = null
      }

      return { success: true }
    } catch (error: any) {
      console.error('Failed to load dashboard data:', error)
      return {
        success: false,
        message: error.message || '加载数据失败'
      }
    } finally {
      loading.value = false
    }
  }

  // 获取仪表盘数据
  const fetchDashboardStats = async () => {
    try {
      const response = await dashboardApi.getStats()

      if (response.success && response.data) {
        stats.value = {
          ...response.data,
          recommendedCourses: normalizeCourseList(response.data.recommendedCourses)
        }
        return { success: true, data: response.data }
      } else {
        return { success: false, message: '获取仪表盘数据失败' }
      }
    } catch (error: any) {
      console.error('Failed to fetch dashboard stats:', error)
      return {
        success: false,
        message: error.message || '获取仪表盘数据失败'
      }
    }
  }

  // 获取学习路径
  const fetchLearningPath = async (courseId: string) => {
    try {
      const response = await dashboardApi.getLearningPath(courseId)

      if (response.success && response.data) {
        learningPath.value = response.data
        return { success: true, data: response.data }
      } else {
        return { success: false, message: '获取学习路径失败' }
      }
    } catch (error: any) {
      console.error('Failed to fetch learning path:', error)
      return {
        success: false,
        message: error.message || '获取学习路径失败'
      }
    }
  }

  // 清除数据
  const clearData = () => {
    stats.value = null
    recentCourses.value = []
    learningPath.value = null
  }

  return {
    // 状态
    stats,
    recentCourses,
    learningPath,
    loading,

    // 方法
    loadDashboardData,
    fetchDashboardStats,
    fetchLearningPath,
    clearData
  }
})

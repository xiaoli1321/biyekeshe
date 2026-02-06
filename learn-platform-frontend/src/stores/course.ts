import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Course, Chapter, CourseSearchParams, PageResponse, CourseProgress, ChapterProgress, CoursePayload } from '@/types'
import { courseApi } from '@/services/api/course'
import { chapterApi } from '@/services/api/chapter'
import { normalizeCourse, normalizeCourseList } from '@/utils/normalize'

export const useCourseStore = defineStore('course', () => {
  // 状态
  const courses = ref<Course[]>([])
  const currentCourse = ref<Course | null>(null)
  const currentChapter = ref<Chapter | null>(null)
  const loading = ref(false)
  const pagination = ref({
    page: 0,
    size: 12,
    totalElements: 0,
    totalPages: 0,
    hasNext: false,
    hasPrevious: false
  })

  const totalPages = computed(() => pagination.value.totalPages || 1)

  // 获取课程列表
  const fetchCourses = async (params?: CourseSearchParams) => {
    try {
      const response = await courseApi.getCourses(params)

      if (response.success && response.data) {
        const pageData: PageResponse<Course> = response.data

        courses.value = normalizeCourseList(pageData.content)

        // 更新分页信息
        pagination.value = {
          page: pageData.currentPage,
          size: pageData.size,
          totalElements: pageData.totalElements,
          totalPages: pageData.totalPages,
          hasNext: pageData.hasNext,
          hasPrevious: pageData.hasPrevious
        }

        return { success: true }
      } else {
        return { success: false, message: '获取课程列表失败' }
      }
    } catch (error: any) {
      console.error('Failed to fetch courses:', error)
      return {
        success: false,
        message: error.message || '获取课程列表失败'
      }
    }
  }

  // 获取课程详情
  const fetchCourseById = async (id: string) => {
    try {
      const response = await courseApi.getCourse(id)

      if (response.success && response.data) {
        currentCourse.value = normalizeCourse(response.data)
        return { success: true, data: currentCourse.value }
      } else {
        return { success: false, message: '获取课程详情失败' }
      }
    } catch (error: any) {
      console.error('Failed to fetch course:', error)
      return {
        success: false,
        message: error.message || '获取课程详情失败'
      }
    }
  }

  // 管理员获取全部课程
  const fetchAllCourses = async () => {
    try {
      const response = await courseApi.getAllCourses()
      if (response.success && response.data) {
        courses.value = normalizeCourseList(response.data)
        return { success: true }
      }
      return { success: false, message: response.message || '获取全部课程失败' }
    } catch (error: any) {
      console.error('Failed to fetch all courses:', error)
      return {
        success: false,
        message: error.message || '获取全部课程失败'
      }
    }
  }

  // 获取课程章节列表
  const fetchCourseChapters = async (courseId: string) => {
    try {
      const response = await courseApi.getChapters(courseId)

      if (response.success && response.data) {
        if (currentCourse.value && currentCourse.value.id === courseId) {
          currentCourse.value = {
            ...currentCourse.value,
            chapters: response.data
          }
        }
        return { success: true, data: response.data }
      } else {
        return { success: false, message: '获取章节列表失败' }
      }
    } catch (error: any) {
      console.error('Failed to fetch course chapters:', error)
      return {
        success: false,
        message: error.message || '获取章节列表失败'
      }
    }
  }

  // 获取章节详情
  const fetchChapterById = async (id: string) => {
    try {
      const response = await chapterApi.getChapter(id)

      if (response.success && response.data) {
        currentChapter.value = response.data
        return { success: true, data: response.data }
      } else {
        currentChapter.value = null
        return { success: false, message: '获取章节详情失败' }
      }
    } catch (error: any) {
      console.error('Failed to fetch chapter:', error)
      currentChapter.value = null
      return {
        success: false,
        message: error.message || '获取章节详情失败'
      }
    }
  }

  // 获取课程学习进度
  const fetchCourseProgress = async (courseId: string) => {
    try {
      const response = await courseApi.getProgress(courseId)
      if (response.success && response.data) {
        return { success: true, data: response.data as CourseProgress }
      }
      return { success: false, message: response.message || '获取课程进度失败' }
    } catch (error: any) {
      console.error('Failed to fetch course progress:', error)
      return {
        success: false,
        message: error.message || '获取课程进度失败'
      }
    }
  }

  // 创建课程
  const createCourse = async (payload: CoursePayload) => {
    try {
      const response = await courseApi.createCourse(payload)
      if (response.success && response.data) {
        return { success: true, data: normalizeCourse(response.data) }
      }
      return { success: false, message: response.message || '创建课程失败' }
    } catch (error: any) {
      console.error('Failed to create course:', error)
      return {
        success: false,
        message: error.message || '创建课程失败'
      }
    }
  }

  // 更新课程
  const updateCourse = async (id: string, payload: CoursePayload) => {
    try {
      const response = await courseApi.updateCourse(id, payload)
      if (response.success && response.data) {
        return { success: true, data: normalizeCourse(response.data) }
      }
      return { success: false, message: response.message || '更新课程失败' }
    } catch (error: any) {
      console.error('Failed to update course:', error)
      return {
        success: false,
        message: error.message || '更新课程失败'
      }
    }
  }

  // 删除课程
  const deleteCourse = async (id: string) => {
    try {
      const response = await courseApi.deleteCourse(id)
      if (response.success) {
        return { success: true }
      }
      return { success: false, message: response.message || '删除课程失败' }
    } catch (error: any) {
      console.error('Failed to delete course:', error)
      return {
        success: false,
        message: error.message || '删除课程失败'
      }
    }
  }

  // 切换发布状态
  const togglePublish = async (id: string) => {
    try {
      const response = await courseApi.togglePublish(id)
      if (response.success && response.data) {
        return { success: true, data: normalizeCourse(response.data) }
      }
      return { success: false, message: response.message || '更新发布状态失败' }
    } catch (error: any) {
      console.error('Failed to toggle publish:', error)
      return {
        success: false,
        message: error.message || '更新发布状态失败'
      }
    }
  }

  // 获取章节学习进度
  const fetchChapterProgress = async (chapterId: string) => {
    try {
      const response = await chapterApi.getProgress(chapterId)
      if (response.success && response.data) {
        return { success: true, data: response.data as ChapterProgress }
      }
      return { success: false, message: response.message || '获取章节进度失败' }
    } catch (error: any) {
      console.error('Failed to fetch chapter progress:', error)
      return {
        success: false,
        message: error.message || '获取章节进度失败'
      }
    }
  }

  // 更新章节学习进度
  const updateChapterProgress = async (chapterId: string, completed: boolean, elapsedMinutes: number) => {
    try {
      const response = await chapterApi.updateProgress(chapterId, { completed, elapsedMinutes })
      if (response.success) {
        return { success: true, message: response.message }
      }
      return { success: false, message: response.message || '更新进度失败' }
    } catch (error: any) {
      console.error('Failed to update chapter progress:', error)
      return {
        success: false,
        message: error.message || '更新进度失败'
      }
    }
  }

  // 搜索课程
  const searchCourses = async (keyword: string, page = 0, size = 12) => {
    try {
      const params: CourseSearchParams = {
        keyword,
        page,
        size
      }

      return await fetchCourses(params)
    } catch (error: any) {
      console.error('Failed to search courses:', error)
      return {
        success: false,
        message: error.message || '搜索课程失败'
      }
    }
  }

  // 获取热门课程
  const fetchPopularCourses = async (limit = 10) => {
    try {
      const response = await courseApi.getPopularCourses(limit)

      if (response.success && response.data) {
        return { success: true, data: normalizeCourseList(response.data) }
      } else {
        return { success: false, message: '获取热门课程失败' }
      }
    } catch (error: any) {
      console.error('Failed to fetch popular courses:', error)
      return {
        success: false,
        message: error.message || '获取热门课程失败'
      }
    }
  }

  // 清除当前课程
  const clearCurrentCourse = () => {
    currentCourse.value = null
  }

  return {
    // 状态
    courses,
    currentCourse,
    currentChapter,
    loading,
    totalPages,

    // 方法
    fetchCourses,
    fetchAllCourses,
    fetchCourseById,
    fetchCourseChapters,
    fetchCourseProgress,
    fetchChapterById,
    fetchChapterProgress,
    updateChapterProgress,
    searchCourses,
    fetchPopularCourses,
    createCourse,
    updateCourse,
    deleteCourse,
    togglePublish,
    clearCurrentCourse
  }
})

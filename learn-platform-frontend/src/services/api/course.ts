import http from '../http'
import type { Course, Chapter, CourseSearchParams, ApiResponse, PageResponse, CourseProgress, CoursePayload } from '@/types'

export const courseApi = {
  // 获取课程列表
  getCourses: (params?: CourseSearchParams): Promise<ApiResponse<PageResponse<Course>>> =>
    http.get('/courses', { params }),

  // 管理员获取全部课程
  getAllCourses: (): Promise<ApiResponse<Course[]>> =>
    http.get('/courses/all'),

  // 获取课程详情
  getCourse: (id: string): Promise<ApiResponse<Course>> =>
    http.get(`/courses/${id}`),

  // 获取课程章节列表
  getChapters: (courseId: string): Promise<ApiResponse<Chapter[]>> =>
    http.get(`/courses/${courseId}/chapters`),

  // 获取课程学习进度
  getProgress: (courseId: string): Promise<ApiResponse<CourseProgress>> =>
    http.get(`/courses/${courseId}/progress`),

  // 创建课程（管理员）
  createCourse: (course: CoursePayload): Promise<ApiResponse<Course>> =>
    http.post('/courses', course),

  // 更新课程（管理员）
  updateCourse: (id: string, course: CoursePayload): Promise<ApiResponse<Course>> =>
    http.put(`/courses/${id}`, course),

  // 删除课程（管理员）
  deleteCourse: (id: string): Promise<ApiResponse<void>> =>
    http.delete(`/courses/${id}`),

  // 切换发布状态（管理员）
  togglePublish: (id: string): Promise<ApiResponse<Course>> =>
    http.post(`/courses/${id}/toggle-publish`),

  // 获取热门课程
  getPopularCourses: (limit = 10): Promise<ApiResponse<Course[]>> =>
    http.get(`/courses/popular?limit=${limit}`),

  // 搜索课程
  searchCourses: (keyword: string, page = 0, size = 12): Promise<ApiResponse<PageResponse<Course>>> =>
    http.get(`/courses/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`),

  // 获取最近课程
  getRecentCourses: (): Promise<ApiResponse<Course[]>> =>
    http.get('/dashboard/recent-courses')
}

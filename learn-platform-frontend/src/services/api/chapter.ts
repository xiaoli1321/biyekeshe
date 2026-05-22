import http from '../http'
import type { Chapter, ApiResponse, ChapterProgress } from '@/types'

export interface ChapterProgressPayload {
  completed: boolean
  elapsedMinutes: number
  status?: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'MASTERED'
}

export interface ChapterPayload {
  courseId: string
  title: string
  description: string
  content: string
  type: 'TEXT' | 'VIDEO' | 'QUIZ' | 'EXERCISE' | 'PROJECT'
  videoUrl?: string
  estimatedMinutes: number
  orderIndex: number
}

export const chapterApi = {
  // 获取章节详情
  getChapter: (id: string): Promise<ApiResponse<Chapter>> =>
    http.get(`/chapters/${id}`),

  // 获取章节进度
  getProgress: (id: string): Promise<ApiResponse<ChapterProgress>> =>
    http.get(`/chapters/${id}/progress`),

  // 更新学习进度
  updateProgress: (id: string, payload: ChapterProgressPayload): Promise<ApiResponse<null>> =>
    http.post(`/chapters/${id}/progress`, payload),

  // 获取章节前置知识点
  getPrerequisites: (id: string): Promise<ApiResponse<any[]>> => 
    http.get(`/chapters/${id}/prerequisites`),

  // 创建章节
  createChapter: (courseId: string, payload: Partial<Chapter>): Promise<ApiResponse<Chapter>> =>
    http.post(`/chapters/course/${courseId}`, payload),

  // 更新章节
  updateChapter: (id: string, payload: Partial<Chapter>): Promise<ApiResponse<Chapter>> =>
    http.put(`/chapters/${id}`, payload),

  // 删除章节
  deleteChapter: (id: string): Promise<ApiResponse<null>> =>
    http.delete(`/chapters/${id}`)
}

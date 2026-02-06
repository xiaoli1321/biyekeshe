import http from '../http'
import type { Chapter, ApiResponse, ChapterProgress } from '@/types'

export interface ChapterProgressPayload {
  completed: boolean
  elapsedMinutes: number
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
    http.post(`/chapters/${id}/progress`, payload)
}

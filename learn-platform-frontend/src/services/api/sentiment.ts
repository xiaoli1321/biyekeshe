import http from '../http'
import type { ApiResponse } from '@/types'

export const sentimentApi = {
  analyze: (): Promise<ApiResponse<any>> =>
    http.post('/sentiment/analyze'),

  listReports: (): Promise<ApiResponse<any>> =>
    http.get('/sentiment/reports'),

  deleteReport: (filename: string): Promise<ApiResponse<any>> =>
    http.delete(`/sentiment/reports/${filename}`)
}

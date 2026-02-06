import http from '../http'
import type { ApiResponse } from '@/types'

export const recommendApi = {
  getShortStock: (limit = 10, minScore = 60): Promise<ApiResponse<any>> =>
    http.get('/recommend/stocks/short', { params: { limit, min_score: minScore } }),

  getLongStock: (limit = 10, minScore = 60): Promise<ApiResponse<any>> =>
    http.get('/recommend/stocks/long', { params: { limit, min_score: minScore } }),

  getShortFund: (limit = 10, minScore = 60): Promise<ApiResponse<any>> =>
    http.get('/recommend/funds/short', { params: { limit, min_score: minScore } }),

  getLongFund: (limit = 10, minScore = 60): Promise<ApiResponse<any>> =>
    http.get('/recommend/funds/long', { params: { limit, min_score: minScore } }),

  getLatest: (): Promise<ApiResponse<any>> =>
    http.get('/recommend/latest')
}

import http from '../http'
import type { ApiResponse } from '@/types'

export const newsApi = {
  getNewsFeed: (category = 'all', page = 1, pageSize = 20): Promise<ApiResponse<any>> =>
    http.get('/news/feed', { params: { category, page, page_size: pageSize } }),

  getHotNews: (limit = 30): Promise<ApiResponse<any>> =>
    http.get('/news/hot', { params: { limit } })
}

import http from '../http'
import type { ApiResponse } from '@/types'

export const stockApi = {
  searchMarketStocks: (query: string): Promise<ApiResponse<any>> =>
    http.get('/market/stocks', { params: { query } }),

  getStockDetails: (code: string): Promise<ApiResponse<any>> =>
    http.get(`/market/stocks/${code}/details`),

  getStockHistory: (code: string): Promise<ApiResponse<any>> =>
    http.get(`/market/stocks/${code}/history`),

  getStockAiDiagnosis: (code: string): Promise<ApiResponse<any>> =>
    http.get(`/stocks/${code}/ai-diagnosis`)
}

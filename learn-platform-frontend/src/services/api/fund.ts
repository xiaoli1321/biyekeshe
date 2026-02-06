import http from '../http'
import type { ApiResponse } from '@/types'

export const fundApi = {
  searchMarketFunds: (query: string): Promise<ApiResponse<any>> =>
    http.get('/market/funds', { params: { q: query } }),

  getFundDetails: (code: string): Promise<ApiResponse<any>> =>
    http.get(`/market/funds/${code}/details`),

  getFundNavHistory: (code: string): Promise<ApiResponse<any>> =>
    http.get(`/market/funds/${code}/nav`),

  getFundDiagnosis: (code: string, forceRefresh?: boolean): Promise<ApiResponse<any>> =>
    http.get(`/funds/${code}/diagnosis`, { params: { forceRefresh } }),

  getFundRiskMetrics: (code: string): Promise<ApiResponse<any>> =>
    http.get(`/funds/${code}/risk-metrics`),

  getFundDrawdownHistory: (code: string, threshold?: number): Promise<ApiResponse<any>> =>
    http.get(`/funds/${code}/drawdown-history`, { params: { threshold } }),

  compareFunds: (codes: string[]): Promise<ApiResponse<any>> =>
    http.post('/funds/compare', { codes })
}

import http from '../http'
import type { ApiResponse } from '@/types'

export interface PortfolioCreatePayload {
  name: string
  description?: string
  benchmark_code?: string
  is_default?: boolean
}

export interface PortfolioUpdatePayload {
  name?: string
  description?: string
  benchmark_code?: string
  is_default?: boolean
}

export interface PositionCreatePayload {
  asset_type: 'fund' | 'stock'
  asset_code: string
  asset_name?: string
  total_shares: number
  average_cost: number
  sector?: string
  notes?: string
}

export interface TransactionCreatePayload {
  asset_type: 'fund' | 'stock'
  asset_code: string
  asset_name?: string
  transaction_type: 'buy' | 'sell' | 'dividend' | 'split' | 'transfer_in' | 'transfer_out'
  shares: number
  price: number
  total_amount?: number
  fees?: number
  transaction_date: string
  notes?: string
}

export const portfolioApi = {
  listPortfolios: (): Promise<ApiResponse<Record<string, any>>> =>
    http.get('/portfolios'),

  getDefaultPortfolio: (): Promise<ApiResponse<Record<string, any>>> =>
    http.get('/portfolios/default'),

  getPortfolio: (portfolioId: number): Promise<ApiResponse<Record<string, any>>> =>
    http.get(`/portfolios/${portfolioId}`),

  createPortfolio: (payload: PortfolioCreatePayload): Promise<ApiResponse<Record<string, any>>> =>
    http.post('/portfolios', payload),

  updatePortfolio: (portfolioId: number, payload: PortfolioUpdatePayload): Promise<ApiResponse<Record<string, any>>> =>
    http.put(`/portfolios/${portfolioId}`, payload),

  deletePortfolio: (portfolioId: number): Promise<ApiResponse<Record<string, any>>> =>
    http.delete(`/portfolios/${portfolioId}`),

  setDefaultPortfolio: (portfolioId: number): Promise<ApiResponse<Record<string, any>>> =>
    http.post(`/portfolios/${portfolioId}/set-default`),

  getPositions: (portfolioId: number, assetType?: string): Promise<ApiResponse<Record<string, any>>> =>
    http.get(`/portfolios/${portfolioId}/positions`, {
      params: assetType ? { assetType } : undefined
    }),

  createPosition: (portfolioId: number, payload: PositionCreatePayload): Promise<ApiResponse<Record<string, any>>> =>
    http.post(`/portfolios/${portfolioId}/positions`, payload),

  deletePosition: (portfolioId: number, positionId: number): Promise<ApiResponse<Record<string, any>>> =>
    http.delete(`/portfolios/${portfolioId}/positions/${positionId}`),

  getTransactions: (
    portfolioId: number,
    assetType?: string,
    limit?: number,
    offset?: number
  ): Promise<ApiResponse<Record<string, any>>> =>
    http.get(`/portfolios/${portfolioId}/transactions`, {
      params: {
        assetType,
        limit,
        offset
      }
    }),

  createTransaction: (portfolioId: number, payload: TransactionCreatePayload): Promise<ApiResponse<Record<string, any>>> =>
    http.post(`/portfolios/${portfolioId}/transactions`, payload),

  deleteTransaction: (portfolioId: number, transactionId: number): Promise<ApiResponse<Record<string, any>>> =>
    http.delete(`/portfolios/${portfolioId}/transactions/${transactionId}`),

  getSummary: (portfolioId: number): Promise<ApiResponse<Record<string, any>>> =>
    http.get(`/portfolios/${portfolioId}/summary`)
}

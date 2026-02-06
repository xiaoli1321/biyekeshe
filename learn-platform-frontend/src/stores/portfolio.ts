import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Portfolio, PortfolioPosition, PortfolioSummary, PortfolioTransaction } from '@/types'
import {
  portfolioApi,
  type PortfolioCreatePayload,
  type PortfolioUpdatePayload,
  type PositionCreatePayload,
  type TransactionCreatePayload
} from '@/services/api/portfolio'

export const usePortfolioStore = defineStore('portfolio', () => {
  const portfolios = ref<Portfolio[]>([])
  const activePortfolioId = ref<number | null>(null)
  const summary = ref<PortfolioSummary | null>(null)
  const positions = ref<PortfolioPosition[]>([])
  const transactions = ref<PortfolioTransaction[]>([])
  const loading = ref(false)

  const setActivePortfolio = (portfolioId: number | null) => {
    activePortfolioId.value = portfolioId
  }

  const loadPortfolios = async () => {
    try {
      loading.value = true
      const response = await portfolioApi.listPortfolios()
      if (response.success) {
        const data = response.data as any
        portfolios.value = Array.isArray(data?.portfolios) ? data.portfolios : []
        return { success: true, data: portfolios.value }
      }
      return { success: false, message: response.message || '获取组合列表失败' }
    } catch (error: any) {
      console.error('Failed to load portfolios:', error)
      return { success: false, message: error?.message || '获取组合列表失败' }
    } finally {
      loading.value = false
    }
  }

  const ensureDefaultPortfolio = async () => {
    try {
      const response = await portfolioApi.getDefaultPortfolio()
      if (response.success && response.data) {
        const data = response.data as Portfolio
        if (data?.id) {
          activePortfolioId.value = data.id
        }
        await loadPortfolios()
        return { success: true, data }
      }
      return { success: false, message: response.message || '获取默认组合失败' }
    } catch (error: any) {
      console.error('Failed to ensure default portfolio:', error)
      return { success: false, message: error?.message || '获取默认组合失败' }
    }
  }

  const createPortfolio = async (payload: PortfolioCreatePayload) => {
    try {
      const response = await portfolioApi.createPortfolio(payload)
      if (response.success) {
        return { success: true, data: response.data }
      }
      return { success: false, message: response.message || '创建组合失败' }
    } catch (error: any) {
      console.error('Failed to create portfolio:', error)
      return { success: false, message: error?.message || '创建组合失败' }
    }
  }

  const updatePortfolio = async (portfolioId: number, payload: PortfolioUpdatePayload) => {
    try {
      const response = await portfolioApi.updatePortfolio(portfolioId, payload)
      if (response.success) {
        return { success: true, data: response.data }
      }
      return { success: false, message: response.message || '更新组合失败' }
    } catch (error: any) {
      console.error('Failed to update portfolio:', error)
      return { success: false, message: error?.message || '更新组合失败' }
    }
  }

  const deletePortfolio = async (portfolioId: number) => {
    try {
      const response = await portfolioApi.deletePortfolio(portfolioId)
      if (response.success) {
        return { success: true }
      }
      return { success: false, message: response.message || '删除组合失败' }
    } catch (error: any) {
      console.error('Failed to delete portfolio:', error)
      return { success: false, message: error?.message || '删除组合失败' }
    }
  }

  const setDefaultPortfolio = async (portfolioId: number) => {
    try {
      const response = await portfolioApi.setDefaultPortfolio(portfolioId)
      if (response.success) {
        return { success: true }
      }
      return { success: false, message: response.message || '设置默认组合失败' }
    } catch (error: any) {
      console.error('Failed to set default portfolio:', error)
      return { success: false, message: error?.message || '设置默认组合失败' }
    }
  }

  const loadSummary = async (portfolioId: number) => {
    try {
      const response = await portfolioApi.getSummary(portfolioId)
      if (response.success) {
        summary.value = response.data as PortfolioSummary
        return { success: true, data: summary.value }
      }
      summary.value = null
      return { success: false, message: response.message || '获取组合汇总失败' }
    } catch (error: any) {
      console.error('Failed to load summary:', error)
      summary.value = null
      return { success: false, message: error?.message || '获取组合汇总失败' }
    }
  }

  const loadPositions = async (portfolioId: number) => {
    try {
      const response = await portfolioApi.getPositions(portfolioId)
      if (response.success) {
        const data = response.data as any
        positions.value = Array.isArray(data?.positions) ? data.positions : []
        return { success: true, data: positions.value }
      }
      positions.value = []
      return { success: false, message: response.message || '获取持仓失败' }
    } catch (error: any) {
      console.error('Failed to load positions:', error)
      positions.value = []
      return { success: false, message: error?.message || '获取持仓失败' }
    }
  }

  const loadTransactions = async (portfolioId: number) => {
    try {
      const response = await portfolioApi.getTransactions(portfolioId)
      if (response.success) {
        const data = response.data as any
        transactions.value = Array.isArray(data?.transactions) ? data.transactions : []
        return { success: true, data: transactions.value }
      }
      transactions.value = []
      return { success: false, message: response.message || '获取交易记录失败' }
    } catch (error: any) {
      console.error('Failed to load transactions:', error)
      transactions.value = []
      return { success: false, message: error?.message || '获取交易记录失败' }
    }
  }

  const refreshPortfolioData = async (portfolioId: number) => {
    await Promise.all([
      loadSummary(portfolioId),
      loadPositions(portfolioId),
      loadTransactions(portfolioId)
    ])
  }

  const createPosition = async (portfolioId: number, payload: PositionCreatePayload) => {
    try {
      const response = await portfolioApi.createPosition(portfolioId, payload)
      if (response.success) {
        return { success: true, data: response.data }
      }
      return { success: false, message: response.message || '新增持仓失败' }
    } catch (error: any) {
      console.error('Failed to create position:', error)
      return { success: false, message: error?.message || '新增持仓失败' }
    }
  }

  const deletePosition = async (portfolioId: number, positionId: number) => {
    try {
      const response = await portfolioApi.deletePosition(portfolioId, positionId)
      if (response.success) {
        return { success: true }
      }
      return { success: false, message: response.message || '删除持仓失败' }
    } catch (error: any) {
      console.error('Failed to delete position:', error)
      return { success: false, message: error?.message || '删除持仓失败' }
    }
  }

  const createTransaction = async (portfolioId: number, payload: TransactionCreatePayload) => {
    try {
      const response = await portfolioApi.createTransaction(portfolioId, payload)
      if (response.success) {
        return { success: true, data: response.data }
      }
      return { success: false, message: response.message || '新增交易失败' }
    } catch (error: any) {
      console.error('Failed to create transaction:', error)
      return { success: false, message: error?.message || '新增交易失败' }
    }
  }

  const deleteTransaction = async (portfolioId: number, transactionId: number) => {
    try {
      const response = await portfolioApi.deleteTransaction(portfolioId, transactionId)
      if (response.success) {
        return { success: true }
      }
      return { success: false, message: response.message || '删除交易失败' }
    } catch (error: any) {
      console.error('Failed to delete transaction:', error)
      return { success: false, message: error?.message || '删除交易失败' }
    }
  }

  return {
    portfolios,
    activePortfolioId,
    summary,
    positions,
    transactions,
    loading,
    setActivePortfolio,
    loadPortfolios,
    ensureDefaultPortfolio,
    createPortfolio,
    updatePortfolio,
    deletePortfolio,
    setDefaultPortfolio,
    loadSummary,
    loadPositions,
    loadTransactions,
    refreshPortfolioData,
    createPosition,
    deletePosition,
    createTransaction,
    deleteTransaction
  }
})

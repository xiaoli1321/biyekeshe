import { defineStore } from 'pinia'
import { ref } from 'vue'
import { agentApi, Agent } from '@/services/api/agent'

export const useAgentStore = defineStore('agent', () => {
  const myAgents = ref<Agent[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const fetchMyAgents = async () => {
    try {
      loading.value = true
      const response = await agentApi.getMyAgents() as any
      myAgents.value = response
      error.value = null
    } catch (err) {
      console.error('Failed to fetch my agents', err)
      error.value = '无法获取智能体列表'
    } finally {
      loading.value = false
    }
  }

  // Helper to get agent by ID from local state if available
  const getAgentById = (id: string) => {
    return myAgents.value.find(a => a.id === id)
  }

  return {
    myAgents,
    loading,
    error,
    fetchMyAgents,
    getAgentById
  }
})

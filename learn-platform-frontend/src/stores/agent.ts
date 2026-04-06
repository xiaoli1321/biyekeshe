import { defineStore } from 'pinia'
import { ref } from 'vue'
import { agentApi, Agent } from '@/services/api/agent'

export const useAgentStore = defineStore('agent', () => {
  const myAgents = ref<Agent[]>([])
  const allAgents = ref<Agent[]>([])
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

  const fetchAllAgents = async () => {
    try {
      loading.value = true
      const response = await agentApi.getAgents() as any
      allAgents.value = response
      error.value = null
    } catch (err) {
      console.error('Failed to fetch all agents', err)
    } finally {
      loading.value = false
    }
  }

  // Helper to get agent by ID from local state if available
  const getAgentById = (id: string) => {
    return myAgents.value.find(a => a.id === id) || allAgents.value.find(a => a.id === id)
  }

  return {
    myAgents,
    allAgents,
    loading,
    error,
    fetchMyAgents,
    fetchAllAgents,
    getAgentById
  }
})

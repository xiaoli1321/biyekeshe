import http from '@/services/http'

export interface Agent {
  id: string
  name: string
  description: string
  systemPrompt: string
  welcomeMessage?: string
  avatar?: string
  modelId: string
  enabled: boolean
  userId: string
  createdAt: string
  lastModifiedAt: string
}

export const agentApi = {
  getAgents() {
    return http.get<Agent[]>('/agents')
  },
  getMyAgents() {
    return http.get<Agent[]>('/agents/my')
  },
  createAgent(agent: Partial<Agent>) {
    return http.post<Agent>('/agents', agent)
  },
  updateAgent(id: string, agent: Partial<Agent>) {
    return http.put<Agent>(`/agents/${id}`, agent)
  },
  deleteAgent(id: string) {
    return http.delete(`/agents/${id}`)
  }
}

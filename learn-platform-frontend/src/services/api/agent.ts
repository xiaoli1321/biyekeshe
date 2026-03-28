import http from '@/services/http'

export interface Agent {
  id: string
  name: string
  description: string
  systemPrompt: string
  welcomeMessage?: string
  avatar?: string
  modelId: string
  kbCollectionId?: string
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
  },
  getConversations(agentId: string) {
    return http.get<Conversation[]>(`/agents/${agentId}/conversations`)
  },
  getMessages(conversationId: string) {
    return http.get<Message[]>(`/agents/conversations/${conversationId}/messages`)
  },
  startConversation(agentId: string, title?: string) {
    return http.post<Conversation>(`/agents/${agentId}/conversations`, { title })
  },
  chatStream(agentId: string, conversationId: string, message: string, onToken: (token: string) => void) {
    const token = localStorage.getItem('token') || ''
    const url = `${import.meta.env.VITE_API_BASE_URL || '/api'}/agents/${agentId}/chat-stream?conversationId=${conversationId}&message=${encodeURIComponent(message)}&token=${token}`
    const eventSource = new EventSource(url)

    eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        if (data.type === 'token') {
          onToken(data.content)
        } else if (data.type === 'error') {
          console.error('SSE Error Event:', data.content)
          eventSource.close()
        }
      } catch (e) {
        console.warn('SSE Parse warning (might be non-JSON heartbeat):', event.data, e)
      }
    }

    eventSource.onerror = (err) => {
      console.error('EventSource failed:', err)
      eventSource.close()
    }

    return eventSource
  }
}

export interface Conversation {
  id: string
  agentId: string
  userId: string
  title: string
  createdAt: string
  lastMessageAt: string
}

export interface Message {
  id: string
  conversationId: string
  role: 'user' | 'assistant' | 'system'
  content: string
  createdAt: string
}

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

export interface AiChatParams {
  chapterId: string
  message: string
  history?: Array<{
    role: 'user' | 'assistant'
    content: string
  }>
}

export type AiStreamHandler = (token: string) => void

export const aiApi = {
  /**
   * 使用 fetch 获取 AI 聊天流
   * 由于 axios 不原生支持 ReadableStream，这里直接使用原生 fetch
   */
  fetchAiChatStream: async (params: AiChatParams, onToken: AiStreamHandler, onComplete?: () => void) => {
    const token = localStorage.getItem('token')
    
    try {
      const response = await fetch(`${BASE_URL}/ai/assistant/chat`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': token ? `Bearer ${token}` : ''
        },
        body: JSON.stringify(params)
      })

      if (!response.ok) {
        throw new Error('AI 服务调用失败')
      }

      const reader = response.body?.getReader()
      if (!reader) return

      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        
        // 处理 SSE 格式: data: {"type":"token","content":"..."}\n\n
        const lines = buffer.split('\n\n')
        buffer = lines.pop() || '' // 最后一个可能是不完整的

        for (const line of lines) {
          if (line.startsWith('data: ')) {
            try {
              const data = JSON.parse(line.slice(6))
              if (data.type === 'token') {
                onToken(data.content)
              }
            } catch (e) {
              console.warn('解析 SSE token 失败', e)
            }
          }
        }
      }
      
      if (onComplete) onComplete()
    } catch (error) {
      console.error('AI Stream Error:', error)
      throw error
    }
  }
}

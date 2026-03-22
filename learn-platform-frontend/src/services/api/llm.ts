import http from '@/services/http'

export interface LlmProviderDto {
  id: string
  name: string
  modelAlias: string
  isCustom: boolean
  baseUrl: string
}

export interface LlmProviderRequest {
  name: string
  modelAlias: string
  apiKey: string
  baseUrl: string
}

export const getLlmProviders = async (): Promise<LlmProviderDto[]> => {
  return await http.get('/llm-providers') as any
}

export const createLlmProvider = async (req: LlmProviderRequest): Promise<LlmProviderDto> => {
  return await http.post('/llm-providers', req) as any
}

export const deleteLlmProvider = async (id: string): Promise<void> => {
  await http.delete(`/llm-providers/${id}`)
}

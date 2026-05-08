import http from '../http'
import type { ApiResponse } from '@/types'

export interface GraphNode {
  id: string
  name: string
  description?: string
  difficultyLevel: number
  importanceWeight: number
  category?: string
  progressStatus?: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'MASTERED'
  chapterId?: string
  chapterTitle?: string
  resourceCount?: number
  centrality?: number
}

export interface GraphLink {
  source: string
  target: string
  type: string
  weight: number
  description?: string
}

export interface GraphStatistics {
  totalNodes: number
  totalLinks: number
  completedNodes: number
  inProgressNodes: number
  isolatedNodeIds: string[]
  isolatedCount: number
  hasCycles?: boolean
  cycles?: string[][]
  centralityMap?: Record<string, number>
}

export interface GraphData {
  nodes: GraphNode[]
  links: GraphLink[]
  statistics?: GraphStatistics
}

export interface ConceptContext {
  concept: {
    id: string
    name: string
    description?: string
    difficultyLevel: number
    importanceWeight: number
  }
  prerequisites: Array<{ id: string; name: string; difficultyLevel: number }>
  dependents: Array<{ id: string; name: string; difficultyLevel: number }>
  related: Array<{ id: string; name: string; difficultyLevel: number }>
  chapterTitle?: string
  courseId?: string
  courseName?: string
}

export const graphApi = {
  getFullGraph: (courseId: string): Promise<ApiResponse<GraphData>> =>
    http.get(`/graphs/${courseId}/full`),

  getConceptGraph: (courseId: string): Promise<ApiResponse<GraphData>> =>
    http.get(`/graphs/concepts/${courseId}`),

  getShortestPath: (from: string, to: string): Promise<ApiResponse<string[]>> =>
    http.get('/graphs/shortest-path', { params: { from, to } }),

  getCentrality: (courseId: string): Promise<ApiResponse<Record<string, number>>> =>
    http.get(`/graphs/${courseId}/centrality`),

  getCycles: (courseId: string): Promise<ApiResponse<string[][]>> =>
    http.get(`/graphs/${courseId}/cycles`),

  getIsolatedConcepts: (courseId: string): Promise<ApiResponse<string[]>> =>
    http.get(`/graphs/${courseId}/isolated`),

  getConceptDetail: (conceptId: string): Promise<ApiResponse<ConceptContext>> =>
    http.get(`/graphs/concept/${conceptId}/detail`),

  getLearningPath: (courseId: string): Promise<ApiResponse<Array<{ id: string; name: string; difficultyLevel: number; importanceWeight: number }>>> =>
    http.get(`/graphs/learning-path/${courseId}`),

  getPrerequisites: (conceptId: string): Promise<ApiResponse<Array<{ id: string; name: string; difficultyLevel: number }>>> =>
    http.get(`/graphs/concept/${conceptId}/prerequisites`),

  searchRelated: (conceptName: string, courseId: string): Promise<ApiResponse<string[]>> =>
    http.get('/graphs/related-concepts', { params: { conceptName, courseId } })
}

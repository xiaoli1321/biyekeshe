// 主要类型定义

// 用户相关
export interface User {
  id: string
  username: string
  email: string
  role: 'USER' | 'ADMIN'
  createdAt?: string
  enabled: boolean
  // 为前端组件提供 currentUser 别名
}

export interface LoginCredentials {
  email: string
  password: string
}

export interface RegisterData {
  username: string
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  user: User
}

// 课程相关
export interface Course {
  id: string
  title: string
  description: string
  name?: string  // 兼容性
  instructor: string
  difficulty?: string
  difficultyLevel?: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'
  estimatedHours: number
  tags: string
  published: boolean
  enrollmentCount?: number
  chapters?: Chapter[]
}

export interface CoursePayload {
  name: string
  description?: string
  instructor?: string
  difficultyLevel?: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'
  estimatedHours?: number
  tags?: string
  published?: boolean
}

export interface Chapter {
  id: string
  courseId: string
  title: string
  description: string
  content: string
  orderIndex: number
  estimatedMinutes: number
}

export interface CourseProgress {
  courseId: string
  totalChapters: number
  completedChapters: number
  completionRate: number
  completedChapterIds: string[]
  inProgressChapterIds: string[]
}

export interface ChapterProgress {
  chapterId: string
  status: string
  completed: boolean
  studyMinutes: number
  completedAt?: string | null
}

export interface CourseSearchParams {
  keyword?: string
  category?: string
  sortBy?: string
  difficulty?: Course['difficultyLevel']
  page?: number
  size?: number
}

// 仪表盘相关
export interface DashboardStats {
  totalCourses: number
  completedChapters: number
  totalHours: number
  recommendedCourses?: Course[]
}

export interface LearningPath {
  courseId: string
  courseName: string
  path: Chapter[]
  suggestions: string[]
}

// 通用相关
export interface ApiResponse<T = any> {
  success: boolean
  data?: T
  message?: string
  errorCode?: string
  timestamp?: number
}

export interface PageResponse<T = any> {
  content: T[]
  totalElements: number
  totalPages: number
  currentPage: number
  size: number
  hasNext: boolean
  hasPrevious: boolean
  isFirst: boolean
  isLast: boolean
}

// 知识图谱相关
export interface ConceptNode {
  id: string
  label: string
  description: string
  category: string
  level: number
}

export interface RelationEdge {
  id: string
  source: string
  target: string
  label: string
  type: string
}

export interface ConceptGraph {
  nodes: ConceptNode[]
  edges: RelationEdge[]
}

// 学习进度相关
export interface LearningProgress {
  chapterId: string
  completed: boolean
  elapsedMinutes: number
  lastAccessed: string
}

// 基金投资组合相关
export interface Portfolio {
  id: number
  name: string
  description?: string
  benchmark_code?: string
  is_default?: boolean | number
  created_at?: string
  updated_at?: string
}

export interface PortfolioPosition {
  id: number
  portfolio_id?: number
  asset_type: 'fund' | 'stock'
  asset_code: string
  asset_name?: string
  total_shares: number
  average_cost: number
  total_cost?: number
  current_price?: number
  current_value?: number
  unrealized_pnl?: number
  unrealized_pnl_pct?: number
  sector?: string
  notes?: string
}

export interface PortfolioTransaction {
  id: number
  portfolio_id?: number
  asset_type: 'fund' | 'stock'
  asset_code: string
  asset_name?: string
  transaction_type: 'buy' | 'sell' | 'dividend' | 'split' | 'transfer_in' | 'transfer_out'
  shares: number
  price: number
  total_amount: number
  fees?: number
  transaction_date: string
  notes?: string
}

export interface PortfolioSummary {
  portfolio?: Portfolio
  total_value?: number
  total_cost?: number
  total_pnl?: number
  total_pnl_pct?: number
  positions_count?: number
  allocation?: {
    by_type?: Record<string, number>
    by_sector?: Record<string, number>
  }
}

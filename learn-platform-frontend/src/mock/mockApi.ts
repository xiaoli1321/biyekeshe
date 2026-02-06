import type { User, Course, Chapter, ApiResponse } from '@/types'

// Mock users data
const MOCK_USERS: User[] = [
  {
    id: '1',
    username: 'testuser',
    email: 'test@example.com',
    role: 'USER',
    enabled: true
  }
]

// Mock courses data
const MOCK_COURSES: Course[] = [
  {
    id: '1',
    title: 'JavaScript 基础教程',
    description: '学习JavaScript编程语言的基础知识，从变量、函数到面向对象编程',
    instructor: '张老师',
    difficulty: '初级',
    estimatedHours: 20,
    tags: 'javascript,frontend,programming',
    published: true,
    enrollmentCount: 1250,
    chapters: [
      {
        id: '1',
        courseId: '1',
        title: 'JavaScript 简介',
        description: '了解JavaScript的历史和基本概念',
        content: '<h2>JavaScript简介</h2><p>JavaScript是一种动态的、解释型的编程语言...</p>',
        orderIndex: 1,
        estimatedMinutes: 30
      },
      {
        id: '2',
        courseId: '1',
        title: '变量和数据类型',
        description: '学习JavaScript中的变量和数据类型',
        content: '<h2>变量和数据类型</h2><p>在JavaScript中，我们可以使用var、let和const声明变量...</p>',
        orderIndex: 2,
        estimatedMinutes: 45
      }
    ]
  },
  {
    id: '2',
    title: 'Vue 3 框架详解',
    description: '掌握Vue 3框架的核心概念和实际应用',
    instructor: '李老师',
    difficulty: '中级',
    estimatedHours: 30,
    tags: 'vue,javascript,frontend,framework',
    published: true,
    enrollmentCount: 860,
    chapters: []
  },
  {
    id: '3',
    title: '前端工程化实践',
    description: '学习现代前端开发工具和工程化实践',
    instructor: '王老师',
    difficulty: '高级',
    estimatedHours: 25,
    tags: 'frontend,engineering,tooling',
    published: true,
    enrollmentCount: 420,
    chapters: []
  }
]

// Mock API responses
export const mockApi = {
  // Auth
  login: async (credentials: { email: string; password: string }): Promise<ApiResponse> => {
    await new Promise(resolve => setTimeout(resolve, 500)) // Simulate network delay
    const user = MOCK_USERS.find(u => u.email === credentials.email)
    if (user) {
      return {
        success: true,
        data: {
          token: 'mock-jwt-token-' + Date.now(),
          user
        }
      }
    }
    return {
      success: false,
      message: '邮箱或密码错误'
    }
  },

  register: async (data: { username: string; email: string; password: string }): Promise<ApiResponse> => {
    await new Promise(resolve => setTimeout(resolve, 500))
    return {
      success: true,
      message: '注册成功，请登录'
    }
  },

  logout: async (): Promise<ApiResponse> => {
    await new Promise(resolve => setTimeout(resolve, 200))
    return {
      success: true,
      message: '已退出登录'
    }
  },

  // Courses
  getCourses: async (params?: any): Promise<ApiResponse> => {
    await new Promise(resolve => setTimeout(resolve, 300))
    return {
      success: true,
      data: {
        content: MOCK_COURSES,
        totalElements: MOCK_COURSES.length,
        totalPages: 1,
        currentPage: 0,
        size: 12,
        hasNext: false,
        hasPrevious: false,
        isFirst: true,
        isLast: true
      }
    }
  },

  getCourse: async (id: string): Promise<ApiResponse> => {
    await new Promise(resolve => setTimeout(resolve, 300))
    const course = MOCK_COURSES.find(c => c.id === id)
    if (course) {
      return {
        success: true,
        data: course
      }
    }
    return {
      success: false,
      message: '课程不存在'
    }
  },

  getRecentCourses: async (): Promise<ApiResponse> => {
    await new Promise(resolve => setTimeout(resolve, 300))
    return {
      success: true,
      data: MOCK_COURSES.slice(0, 3)
    }
  },

  // Dashboard
  getDashboardStats: async (): Promise<ApiResponse> => {
    await new Promise(resolve => setTimeout(resolve, 300))
    return {
      success: true,
      data: {
        totalCourses: MOCK_COURSES.length,
        completedCourses: 1,
        learningProgress: 65
      }
    }
  },

  getLearningPath: async (courseId: string): Promise<ApiResponse> => {
    await new Promise(resolve => setTimeout(resolve, 300))
    return {
      success: true,
      data: [
        {
          courseId: '1',
          courseTitle: 'JavaScript 基础教程',
          path: [],
          suggestions: ['完成所有章节练习', '完成项目实战']
        },
        {
          courseId: '2',
          courseTitle: 'Vue 3 框架详解',
          path: [],
          suggestions: ['掌握Vue 3组合式API', '学习Vue Router和Pinia']
        }
      ]
    }
  }
}

export default mockApi
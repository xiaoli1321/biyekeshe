import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// Lazy load components
const Login = () => import('@/views/Login.vue')
const Register = () => import('@/views/Register.vue')
const Dashboard = () => import('@/views/Dashboard.vue')
const CourseList = () => import('@/views/CourseList.vue')
const CourseDetail = () => import('@/views/CourseDetail.vue')
const CourseManage = () => import('@/views/CourseManage.vue')
const ChapterView = () => import('@/views/ChapterView.vue')
const KnowledgeGraph = () => import('@/views/KnowledgeGraph.vue')
const PortfolioManage = () => import('@/views/PortfolioManage.vue')

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresGuest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { requiresGuest: true }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard,
    meta: { requiresAuth: true }
  },
  {
    path: '/courses',
    name: 'CourseList',
    component: CourseList,
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/courses',
    name: 'CourseManage',
    component: CourseManage,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/courses/:id',
    name: 'CourseDetail',
    component: CourseDetail,
    meta: { requiresAuth: true }
  },
  {
    path: '/chapters/:id',
    name: 'ChapterView',
    component: ChapterView,
    meta: { requiresAuth: true }
  },
  {
    path: '/graph',
    name: 'KnowledgeGraph',
    component: KnowledgeGraph,
    meta: { requiresAuth: true }
  },
  {
    path: '/portfolios',
    name: 'PortfolioManage',
    component: PortfolioManage,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guards
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
    return
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    next('/dashboard')
    return
  }

  if (to.meta.requiresGuest && authStore.isAuthenticated) {
    next('/dashboard')
    return
  }

  next()
})

export default router

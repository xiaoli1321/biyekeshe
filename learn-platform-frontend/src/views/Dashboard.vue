<template>
  <div class="container">
    <div class="row mb-4">
      <div class="col-12">
        <h1 class="display-6">
          <i class="bi bi-speedometer2 me-2"></i>
          学习仪表盘
        </h1>
        <p class="lead text-muted">
          欢迎回来，{{ authStore.currentUser?.username }}！
        </p>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="row mb-4">
      <div class="col-md-4 mb-3">
        <div class="card text-white bg-primary">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <h6 class="card-title">总课程数</h6>
                <h2 class="mb-0">{{ dashboardStore.stats?.totalCourses || 0 }}</h2>
              </div>
              <i class="bi bi-collection fs-1"></i>
            </div>
          </div>
        </div>
      </div>

      <div class="col-md-4 mb-3">
        <div class="card text-white bg-success">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <h6 class="card-title">已完成章节</h6>
                <h2 class="mb-0">{{ dashboardStore.stats?.completedChapters || 0 }}</h2>
              </div>
              <i class="bi bi-check-circle fs-1"></i>
            </div>
          </div>
        </div>
      </div>

      <div class="col-md-4 mb-3">
        <div class="card text-white bg-info">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <h6 class="card-title">学习时长</h6>
                <h2 class="mb-0">{{ dashboardStore.stats?.totalHours || 0 }} 小时</h2>
              </div>
              <i class="bi bi-graph-up fs-1"></i>
            </div>
          </div>
        </div>
      </div>
    </div>

    <Loading v-if="dashboardStore.loading" />

    <div v-else class="row">
      <!-- Finance Quick Entry -->
      <div class="col-12 mb-4">
        <div class="card">
          <div class="card-header bg-white">
            <h5 class="mb-0">
              <i class="bi bi-grid me-2"></i>
              金融功能快捷入口
            </h5>
          </div>
          <div class="card-body">
            <div class="row g-3">
              <div class="col-md-4">
                <router-link to="/finance/portfolios" class="text-decoration-none">
                  <div class="card h-100 border-0 shadow-sm">
                    <div class="card-body d-flex align-items-center gap-3">
                      <i class="bi bi-pie-chart fs-3 text-primary"></i>
                      <div>
                        <div class="fw-semibold">基金组合</div>
                        <small class="text-muted">管理组合与持仓</small>
                      </div>
                    </div>
                  </div>
                </router-link>
              </div>
              <div class="col-md-4">
                <router-link to="/finance/fund-analysis" class="text-decoration-none">
                  <div class="card h-100 border-0 shadow-sm">
                    <div class="card-body d-flex align-items-center gap-3">
                      <i class="bi bi-bar-chart-line fs-3 text-success"></i>
                      <div>
                        <div class="fw-semibold">基金分析</div>
                        <small class="text-muted">净值与风险指标</small>
                      </div>
                    </div>
                  </div>
                </router-link>
              </div>
              <div class="col-md-4">
                <router-link to="/finance/stock-monitor" class="text-decoration-none">
                  <div class="card h-100 border-0 shadow-sm">
                    <div class="card-body d-flex align-items-center gap-3">
                      <i class="bi bi-activity fs-3 text-warning"></i>
                      <div>
                        <div class="fw-semibold">股票监控</div>
                        <small class="text-muted">行情与AI诊断</small>
                      </div>
                    </div>
                  </div>
                </router-link>
              </div>
              <div class="col-md-4">
                <router-link to="/finance/recommendations" class="text-decoration-none">
                  <div class="card h-100 border-0 shadow-sm">
                    <div class="card-body d-flex align-items-center gap-3">
                      <i class="bi bi-stars fs-3 text-info"></i>
                      <div>
                        <div class="fw-semibold">AI推荐</div>
                        <small class="text-muted">短期/长期推荐</small>
                      </div>
                    </div>
                  </div>
                </router-link>
              </div>
              <div class="col-md-4">
                <router-link to="/finance/news" class="text-decoration-none">
                  <div class="card h-100 border-0 shadow-sm">
                    <div class="card-body d-flex align-items-center gap-3">
                      <i class="bi bi-newspaper fs-3 text-secondary"></i>
                      <div>
                        <div class="fw-semibold">新闻资讯</div>
                        <small class="text-muted">热点与研报</small>
                      </div>
                    </div>
                  </div>
                </router-link>
              </div>
              <div class="col-md-4">
                <router-link to="/finance/sentiment" class="text-decoration-none">
                  <div class="card h-100 border-0 shadow-sm">
                    <div class="card-body d-flex align-items-center gap-3">
                      <i class="bi bi-emoji-smile fs-3 text-danger"></i>
                      <div>
                        <div class="fw-semibold">情绪分析</div>
                        <small class="text-muted">市场情绪报告</small>
                      </div>
                    </div>
                  </div>
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Courses -->
      <div class="col-lg-8 mb-4">
        <div class="card">
          <div class="card-header bg-white">
            <h5 class="mb-0">
              <i class="bi bi-collection me-2"></i>
              推荐课程
            </h5>
          </div>
          <div class="card-body">
            <div v-if="dashboardStore.recentCourses?.length === 0" class="text-center py-5">
              <i class="bi bi-collection fs-1 text-muted"></i>
              <p class="text-muted mt-3">暂无推荐课程</p>
              <router-link to="/courses" class="btn btn-primary">
                <i class="bi bi-search me-2"></i>
                浏览课程
              </router-link>
            </div>

            <div v-else class="row">
              <div
                v-for="course in dashboardStore.recentCourses"
                :key="course.id"
                class="col-md-6 mb-3"
              >
                <div class="card h-100">
                  <div class="card-body">
                    <h6 class="card-title">{{ course.title }}</h6>
                    <p class="card-text text-muted small">
                      {{ course.description?.substring(0, 100) }}...
                    </p>
                    <div class="progress mb-2" style="height: 5px;">
                      <div
                        class="progress-bar"
                        role="progressbar"
                        :style="{ width: `${getProgress(course.id)}%` }"
                      ></div>
                    </div>
                    <router-link
                      :to="`/courses/${course.id}`"
                      class="btn btn-sm btn-outline-primary"
                    >
                      继续学习
                    </router-link>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Learning Path -->
      <div class="col-lg-4 mb-4">
        <div class="card">
          <div class="card-header bg-white">
            <h5 class="mb-0">
              <i class="bi bi-map me-2"></i>
              学习路径
            </h5>
          </div>
          <div class="card-body">
            <div v-if="!dashboardStore.learningPath" class="text-center py-3">
              <p class="text-muted">正在生成学习路径...</p>
            </div>
            <div v-else>
              <p class="text-muted small mb-2">
                课程：{{ dashboardStore.learningPath.courseName }}
              </p>
              <ol class="list-group list-group-flush">
                <li
                  v-for="(step, index) in dashboardStore.learningPath.path"
                  :key="index"
                  class="list-group-item px-0"
                >
                  <div class="d-flex align-items-center">
                    <span class="badge rounded-pill bg-primary me-2">{{ index + 1 }}</span>
                    <span>{{ step.title }}</span>
                  </div>
                </li>
              </ol>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useDashboardStore } from '@/stores/dashboard'
import Loading from '@/components/Loading.vue'

const authStore = useAuthStore()
const dashboardStore = useDashboardStore()

// Mock progress calculation
const getProgress = (_courseId: string): number => {
  // In real app, this would come from the API
  return Math.floor(Math.random() * 100)
}

onMounted(() => {
  dashboardStore.loadDashboardData()
})
</script>

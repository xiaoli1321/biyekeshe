<template>
  <div class="container">
    <div class="row mb-4">
      <div class="col-12">
        <h1 class="display-6">
          <i class="bi bi-collection me-2"></i>
          课程列表
        </h1>
      </div>
    </div>

    <!-- Search and Filter -->
    <div class="row mb-4">
      <div class="col-md-6">
        <div class="input-group">
          <span class="input-group-text">
            <i class="bi bi-search"></i>
          </span>
          <input
            type="text"
            class="form-control"
            placeholder="搜索课程..."
            v-model="searchQuery"
            @input="handleSearch"
          />
        </div>
      </div>
      <div class="col-md-3">
        <select class="form-select" v-model="selectedCategory" @change="handleFilter">
          <option value="">所有分类</option>
          <option value="programming">编程</option>
          <option value="design">设计</option>
          <option value="business">商业</option>
          <option value="science">科学</option>
        </select>
      </div>
      <div class="col-md-3">
        <select class="form-select" v-model="sortBy" @change="handleSort">
          <option value="title">按标题排序</option>
          <option value="createdAt">按创建时间</option>
          <option value="difficulty">按难度</option>
        </select>
      </div>
    </div>

    <Loading v-if="courseStore.loading" />

    <div v-else>
      <div class="row">
        <div
          v-for="course in courseStore.courses"
          :key="course.id"
          class="col-lg-4 col-md-6 mb-4"
        >
          <div class="card h-100 course-card">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-start mb-2">
                <h5 class="card-title">{{ course.title }}</h5>
                <span class="badge bg-primary">{{ course.difficulty || '初级' }}</span>
              </div>
              <p class="card-text text-muted">
                {{ course.description?.substring(0, 120) }}...
              </p>
              <div class="d-flex justify-content-between align-items-center">
                <small class="text-muted">
                  <i class="bi bi-clock me-1"></i>
                  {{ course.estimatedHours || 10 }} 小时
                </small>
                <small class="text-muted">
                  <i class="bi bi-people me-1"></i>
                  {{ course.enrollmentCount || 0 }} 学员
                </small>
              </div>
            </div>
            <div class="card-footer bg-transparent">
              <div class="d-grid gap-2">
                <router-link
                  :to="`/courses/${course.id}`"
                  class="btn btn-outline-primary btn-sm"
                >
                  <i class="bi bi-eye me-1"></i>
                  查看详情
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="row mt-4">
        <div class="col-12 d-flex justify-content-center">
          <nav v-if="courseStore.totalPages > 1">
            <ul class="pagination">
              <li class="page-item" :class="{ disabled: currentPage === 1 }">
                <a class="page-link" href="#" @click.prevent="handlePageChange(currentPage - 1)">
                  上一页
                </a>
              </li>
              <li
                v-for="page in courseStore.totalPages"
                :key="page"
                class="page-item"
                :class="{ active: page === currentPage }"
              >
                <a class="page-link" href="#" @click.prevent="handlePageChange(page)">
                  {{ page }}
                </a>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === courseStore.totalPages }">
                <a class="page-link" href="#" @click.prevent="handlePageChange(currentPage + 1)">
                  下一页
                </a>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useCourseStore } from '@/stores/course'
import Loading from '@/components/Loading.vue'

const courseStore = useCourseStore()

const searchQuery = ref('')
const selectedCategory = ref('')
const sortBy = ref('title')
const currentPage = ref(1)
const pageSize = ref(12)

const handleSearch = () => {
  currentPage.value = 1
  loadCourses()
}

const handleFilter = () => {
  currentPage.value = 1
  loadCourses()
}

const handleSort = () => {
  currentPage.value = 1
  loadCourses()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadCourses()
}

const loadCourses = async () => {
  await courseStore.fetchCourses({
    keyword: searchQuery.value,
    category: selectedCategory.value,
    sortBy: sortBy.value,
    page: currentPage.value - 1, // 后端是0-indexed
    size: pageSize.value
  })
}

onMounted(() => {
  loadCourses()
})
</script>

<style scoped>
.course-card {
  transition: transform 0.2s, box-shadow 0.2s;
}

.course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}
</style>
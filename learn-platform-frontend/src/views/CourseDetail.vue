<template>
  <div class="container">
    <Loading v-if="courseStore.loading" />

    <div v-else-if="course">
      <!-- Back Button -->
      <div class="row mb-3">
        <div class="col-12">
          <router-link to="/courses" class="btn btn-link">
            <i class="bi bi-arrow-left me-2"></i>
            返回课程列表
          </router-link>
        </div>
      </div>

      <!-- Course Header -->
      <div class="row mb-4">
        <div class="col-12">
          <div class="card">
            <div class="card-body">
              <div class="row">
                <div class="col-md-8">
                  <h1 class="display-6">{{ course.title }}</h1>
                  <p class="lead text-muted">{{ course.description }}</p>
                  <div class="d-flex gap-2 flex-wrap">
                    <span class="badge bg-primary">{{ course.difficulty || '初级' }}</span>
                    <span class="badge bg-secondary">
                      <i class="bi bi-clock me-1"></i>
                      {{ course.estimatedHours || 10 }} 小时
                    </span>
                    <span class="badge bg-info">
                      <i class="bi bi-people me-1"></i>
                      {{ course.enrollmentCount || 0 }} 学员
                    </span>
                  </div>
                </div>
                <div class="col-md-4 text-end">
                  <button
                    class="btn btn-primary btn-lg"
                    @click="enrollInCourse"
                    :disabled="enrolling"
                  >
                    <span v-if="enrolling" class="spinner-border spinner-border-sm me-2"></span>
                    <i v-else class="bi bi-play-circle me-2"></i>
                    {{ enrolled ? '已注册' : '开始学习' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Course Content -->
      <div class="row">
        <div class="col-lg-8">
          <!-- Chapters -->
          <div class="card mb-4">
            <div class="card-header bg-white">
              <h5 class="mb-0">
                <i class="bi bi-list-ul me-2"></i>
                课程章节
              </h5>
            </div>
            <div class="card-body">
              <div v-if="!course.chapters || course.chapters.length === 0" class="text-center py-4">
                <p class="text-muted">暂无章节内容</p>
              </div>
              <div v-else class="list-group list-group-flush">
                <div
                  v-for="(chapter, index) in course.chapters"
                  :key="chapter.id"
                  class="list-group-item d-flex justify-content-between align-items-center px-0"
                >
                  <div class="d-flex align-items-center" style="flex: 1;">
                    <span class="badge rounded-pill bg-light text-dark me-3">
                      {{ index + 1 }}
                    </span>
                    <div>
                      <h6 class="mb-1">{{ chapter.title }}</h6>
                      <small class="text-muted">
                        {{ chapter.description }}
                      </small>
                    </div>
                  </div>
                  <div class="d-flex align-items-center gap-2">
                    <small class="text-muted">
                      {{ chapter.estimatedMinutes || 30 }} 分钟
                    </small>
                    <router-link
                      v-if="enrolled"
                      :to="`/chapters/${chapter.id}`"
                      class="btn btn-sm btn-outline-primary"
                    >
                      <i class="bi bi-play-fill"></i>
                    </router-link>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Sidebar -->
        <div class="col-lg-4">
          <!-- Course Progress -->
          <div class="card mb-4">
            <div class="card-header bg-white">
              <h5 class="mb-0">
                <i class="bi bi-graph-up me-2"></i>
                学习进度
              </h5>
            </div>
            <div class="card-body">
              <div v-if="!enrolled" class="text-center py-3">
                <p class="text-muted">请先注册课程</p>
              </div>
              <div v-else>
                <div class="progress mb-3" style="height: 10px;">
                  <div
                    class="progress-bar"
                    role="progressbar"
                    :style="{ width: `${progress}%` }"
                  ></div>
                </div>
                <p class="text-center text-muted small">
                  已完成 {{ completedChapters }}/{{ totalChapters }} 章节
                </p>
              </div>
            </div>
          </div>

          <!-- Course Info -->
          <div class="card">
            <div class="card-header bg-white">
              <h5 class="mb-0">
                <i class="bi bi-info-circle me-2"></i>
                课程信息
              </h5>
            </div>
            <div class="card-body">
              <dl class="row">
                <dt class="col-sm-5">难度等级:</dt>
                <dd class="col-sm-7">{{ course.difficulty || '初级' }}</dd>

                <dt class="col-sm-5">预计时长:</dt>
                <dd class="col-sm-7">{{ course.estimatedHours || 10 }} 小时</dd>

                <dt class="col-sm-5">课程章节:</dt>
                <dd class="col-sm-7">{{ course.chapters?.length || 0 }} 个</dd>

                <dt class="col-sm-5">注册人数:</dt>
                <dd class="col-sm-7">{{ course.enrollmentCount || 0 }} 人</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useCourseStore } from '@/stores/course'
import Loading from '@/components/Loading.vue'

const route = useRoute()
const courseStore = useCourseStore()

const enrolling = ref(false)
const enrolled = ref(false)
const progress = ref(0)

const course = computed(() => courseStore.currentCourse)
const completedChapters = ref(0)
const totalChapters = computed(() => course.value?.chapters?.length || 0)

const enrollInCourse = async () => {
  if (!course.value) return

  enrolling.value = true
  try {
    // In real app, this would call the API
    enrolled.value = true
    progress.value = 0
  } catch (error) {
    console.error('注册课程失败:', error)
  } finally {
    enrolling.value = false
  }
}

onMounted(async () => {
  const courseId = route.params.id as string
  await courseStore.fetchCourseById(courseId)
  await courseStore.fetchCourseChapters(courseId)
  const progressResult = await courseStore.fetchCourseProgress(courseId)
  if (progressResult?.success && progressResult.data) {
    completedChapters.value = progressResult.data.completedChapters
    progress.value = Math.round(progressResult.data.completionRate || 0)
    enrolled.value = true
  }
})
</script>

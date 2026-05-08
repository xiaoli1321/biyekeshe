<template>
  <div class="container">
    <Loading v-if="loading" />

    <div v-else-if="chapter">
      <!-- Navigation -->
      <div class="row mb-3">
        <div class="col-12">
          <nav aria-label="breadcrumb">
            <ol class="breadcrumb">
              <li class="breadcrumb-item">
                <router-link to="/courses">课程</router-link>
              </li>
              <li class="breadcrumb-item">
                <router-link :to="`/courses/${chapter.courseId}`">
                  {{ courseTitle }}
                </router-link>
              </li>
              <li class="breadcrumb-item active">
                {{ chapter.title }}
              </li>
            </ol>
          </nav>
        </div>
      </div>

      <!-- Chapter Header -->
      <div class="row mb-4">
        <div class="col-12">
          <div class="card">
            <div class="card-body">
              <div class="row align-items-center">
                <div class="col-md-8">
                  <h1 class="h3 mb-2">{{ chapter.title }}</h1>
                  <p class="text-muted mb-0">{{ chapter.description }}</p>
                </div>
                <div class="col-md-4 text-end">
                  <span class="badge bg-light text-dark me-2">
                    <i class="bi bi-clock me-1"></i>
                    预计 {{ chapter.estimatedMinutes || 30 }} 分钟
                  </span>
                  <button
                    class="btn btn-success"
                    @click="markAsCompleted"
                    :disabled="completed || completing"
                  >
                    <span v-if="completing" class="spinner-border spinner-border-sm me-2"></span>
                    <i v-else-if="completed" class="bi bi-check-circle me-2"></i>
                    <i v-else class="bi bi-check me-2"></i>
                    {{ completed ? '已完成' : '标记完成' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Chapter Content -->
      <div class="row">
        <div class="col-lg-8">
          <div class="card">
            <div class="card-body">
              <!-- Video Player (Support File/Bilibili/YouTube) -->
              <div v-if="chapter.type === 'VIDEO' && chapter.videoUrl" class="ratio ratio-16x9 mb-4 shadow-sm rounded overflow-hidden bg-black">
                <template v-if="videoInfo.type === 'BILIBILI' || videoInfo.type === 'YOUTUBE'">
                  <iframe 
                    :src="videoInfo.embedUrl" 
                    scrolling="no" 
                    border="0" 
                    frameborder="no" 
                    framespacing="0" 
                    allowfullscreen="true"
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                  ></iframe>
                </template>
                <template v-else>
                  <video 
                    controls 
                    class="w-100 h-100"
                    :src="videoInfo.embedUrl"
                  >
                    您的浏览器不支持视频播放。
                  </video>
                </template>
              </div>

              <div v-if="chapter.content" v-html="chapter.content"></div>
              <div v-else class="text-muted">暂无章节内容</div>
            </div>
          </div>

          <!-- Progress Update -->
          <div class="card mt-4">
            <div class="card-body">
              <h5>
                <i class="bi bi-broadcast me-2"></i>
                学习进度
              </h5>
              <p class="text-muted">
                在此章节中您学到了什么？
              </p>
              <div class="progress mb-3" style="height: 10px;">
                <div
                  class="progress-bar"
                  role="progressbar"
                  :style="{ width: `${progress}%` }"
                ></div>
              </div>
              <p class="small text-muted">
                当前进度: {{ progress }}%
              </p>
            </div>
          </div>
        </div>

        <!-- Sidebar -->
        <div class="col-lg-4">
          <!-- Chapter Navigation -->
          <div class="card mb-4">
            <div class="card-header bg-white">
              <h5 class="mb-0">
                <i class="bi bi-list-check me-2"></i>
                章节导航
              </h5>
            </div>
            <div class="card-body">
              <div class="list-group list-group-flush">
                <div
                  v-for="(chap, index) in chapters"
                  :key="chap.id"
                  class="list-group-item px-0 py-2"
                >
                  <div class="d-flex justify-content-between align-items-center">
                    <router-link
                      :to="`/chapters/${chap.id}`"
                      class="text-decoration-none flex-grow-1"
                    >
                      <div class="d-flex align-items-center">
                        <span class="badge rounded-pill bg-light text-dark me-2">
                          {{ index + 1 }}
                        </span>
                        <span class="small">{{ chap.title }}</span>
                      </div>
                    </router-link>
                    <i v-if="chap.completed" class="bi bi-check-circle-fill text-success"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Prerequisite Knowledge -->
          <div v-if="prerequisites.length > 0" class="card mt-4">
            <div class="card-header bg-warning-subtle">
              <h5 class="mb-0">
                <i class="bi bi-lightbulb me-2 text-warning"></i>
                学习前置建议
              </h5>
            </div>
            <div class="card-body">
              <p class="small text-muted mb-3">为更好地理解本章，建议您先复习以下知识点：</p>
              <div class="list-group list-group-flush">
                <div
                  v-for="pre in prerequisites"
                  :key="pre.id"
                  class="list-group-item px-0 py-2 border-0"
                >
                  <div class="d-flex align-items-start">
                    <i class="bi bi-arrow-right-short mt-1 me-1 text-primary"></i>
                    <div>
                      <div class="fw-bold small">{{ pre.label }}</div>
                      <div class="text-muted extra-small">{{ pre.description }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- AI Study Buddy -->
    <AiSidekick v-if="chapter" :chapterId="chapter.id" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useCourseStore } from '@/stores/course'
import { chapterApi } from '@/services/api/chapter'
import Loading from '@/components/Loading.vue'
import AiSidekick from '@/components/AiSidekick.vue'
import type { Chapter, ConceptNode } from '@/types'
import { getVideoInfo } from '@/utils/video'

const route = useRoute()
const courseStore = useCourseStore()

const loading = ref(false)
const completing = ref(false)
const completed = ref(false)
const progress = ref(0)

const chapter = computed(() => courseStore.currentChapter)
const chapters = ref<(Chapter & { completed?: boolean })[]>([])
const courseTitle = computed(() => courseStore.currentCourse?.title || courseStore.currentCourse?.name || '课程')
const completedChapterIds = ref<string[]>([])
const prerequisites = ref<ConceptNode[]>([])

const videoInfo = computed(() => {
  if (!chapter.value?.videoUrl) return { type: 'UNKNOWN', embedUrl: '' }
  return getVideoInfo(chapter.value.videoUrl)
})

const markAsCompleted = async () => {
  if (!chapter.value) {
    return
  }

  completing.value = true
  try {
    const minutes = Math.max(0, chapter.value.estimatedMinutes || 0)
    const result = await courseStore.updateChapterProgress(chapter.value.id, true, minutes)
    if (result?.success) {
      completed.value = true
      progress.value = 100
      if (!completedChapterIds.value.includes(chapter.value.id)) {
        completedChapterIds.value.push(chapter.value.id)
      }
      chapters.value = chapters.value.map((item) => ({
        ...item,
        completed: completedChapterIds.value.includes(item.id)
      }))
    } else {
      console.error(result?.message || '更新进度失败')
    }
  } catch (error) {
    console.error('标记完成失败:', error)
  } finally {
    completing.value = false
  }
}

onMounted(async () => {
  loading.value = true
  const chapterId = route.params.id as string

  const chapterResult = await courseStore.fetchChapterById(chapterId)
  if (chapterResult?.success && chapterResult.data?.courseId) {
    await courseStore.fetchCourseById(chapterResult.data.courseId)
    await courseStore.fetchCourseChapters(chapterResult.data.courseId)
    const courseChapters = courseStore.currentCourse?.chapters || []
    chapters.value = courseChapters
      .filter((item) => item.id !== chapterId)
      .map((item) => ({ ...item, completed: false }))

    const courseProgress = await courseStore.fetchCourseProgress(chapterResult.data.courseId)
    if (courseProgress?.success && courseProgress.data) {
      completedChapterIds.value = courseProgress.data.completedChapterIds || []
      chapters.value = chapters.value.map((item) => ({
        ...item,
        completed: completedChapterIds.value.includes(item.id)
      }))
    }

    const chapterProgress = await courseStore.fetchChapterProgress(chapterId)
    if (chapterProgress?.success && chapterProgress.data) {
      completed.value = chapterProgress.data.completed
      const studyMinutes = Math.max(0, chapterProgress.data.studyMinutes || 0)
      const estimatedMinutes = Math.max(1, chapterResult.data.estimatedMinutes || 0)
      progress.value = completed.value
        ? 100
        : Math.min(100, Math.round((studyMinutes / estimatedMinutes) * 100))
    }

    // 获取前置知识点
    const preResult = await chapterApi.getPrerequisites(chapterId)
    if (preResult?.success && preResult.data) {
      prerequisites.value = preResult.data
    }
  } else {
    chapters.value = []
  }

  loading.value = false
})
</script>

<style scoped>
.chapter-content {
  min-height: 400px;
}

.chapter-content :deep(h2) {
  color: var(--primary-color);
  margin-bottom: 1rem;
}

.chapter-content :deep(h3) {
  margin-top: 2rem;
  margin-bottom: 1rem;
}

.chapter-content :deep(code) {
  background-color: #f8f9fa;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
}

.breadcrumb {
  background: none;
  padding: 0;
  margin: 0;
}

.extra-small {
  font-size: 0.75rem;
}
</style>

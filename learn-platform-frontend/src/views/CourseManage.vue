<!-- Managed by Antigravity -->
<template>
  <div class="container">
    <div class="row mb-4">
      <div class="col-12">
        <h1 class="display-6">
          <i class="bi bi-gear me-2"></i>
          课程管理
        </h1>
      </div>
    </div>

    <div v-if="message.text" class="alert" :class="message.type" role="alert">
      <i class="bi bi-info-circle me-2"></i>
      {{ message.text }}
    </div>

    <div class="card mb-4">
      <div class="card-header bg-white">
        <h5 class="mb-0">
          <i class="bi bi-pencil-square me-2"></i>
          {{ editingId ? '编辑课程' : '创建课程' }}
        </h5>
      </div>
      <div class="card-body">
        <form @submit.prevent="handleSubmit">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label">课程名称</label>
              <input v-model="form.name" type="text" class="form-control" required />
            </div>
            <div class="col-md-6">
              <label class="form-label">讲师</label>
              <input v-model="form.instructor" type="text" class="form-control" />
            </div>
            <div class="col-md-12">
              <label class="form-label">课程简介</label>
              <textarea v-model="form.description" class="form-control" rows="2"></textarea>
            </div>
            <div class="col-md-4">
              <label class="form-label">难度等级</label>
              <select v-model="form.difficultyLevel" class="form-select">
                <option value="BEGINNER">初级</option>
                <option value="INTERMEDIATE">中级</option>
                <option value="ADVANCED">高级</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label">预计时长（小时）</label>
              <input v-model.number="form.estimatedHours" type="number" min="0" class="form-control" />
            </div>
            <div class="col-md-4">
              <label class="form-label">标签</label>
              <input v-model="form.tags" type="text" class="form-control" placeholder="Java,编程,入门" />
            </div>
          </div>

          <div class="mt-3 d-flex gap-2">
            <button class="btn btn-primary" type="submit" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
              {{ editingId ? '保存修改' : '创建课程' }}
            </button>
            <button v-if="editingId" class="btn btn-outline-secondary" type="button" @click="resetForm">
              取消编辑
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Chapter Management (Only when editing a course) -->
    <div v-if="editingId" class="card mb-4 border-primary">
      <div class="card-header bg-primary-subtle d-flex justify-content-between align-items-center">
        <h5 class="mb-0 text-primary">
          <i class="bi bi-layers me-2"></i>
          章节内容管理
        </h5>
        <button class="btn btn-primary btn-sm" @click="openChapterModal()">
          <i class="bi bi-plus-lg me-1"></i>
          添加章节
        </button>
      </div>
      <div class="card-body p-0">
        <div v-if="loadingChapters" class="text-center py-4">
          <div class="spinner-border spinner-border-sm text-primary"></div>
        </div>
        <div v-else-if="chapters.length === 0" class="text-center py-4 text-muted small">
          该课程暂无章节，请点击上方按钮添加。
        </div>
        <div v-else class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th width="80">排序</th>
                <th>标题</th>
                <th>类型</th>
                <th>预计时长</th>
                <th class="text-end">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="chap in chapters" :key="chap.id">
                <td class="text-center">{{ chap.orderIndex }}</td>
                <td>
                  <div class="fw-bold">{{ chap.title }}</div>
                  <div class="text-muted extra-small">{{ chap.description }}</div>
                </td>
                <td>
                  <span class="badge" :class="chap.type === 'VIDEO' ? 'bg-info' : 'bg-secondary'">
                    {{ chap.type === 'VIDEO' ? '视频' : '文本' }}
                  </span>
                </td>
                <td>{{ chap.estimatedMinutes }}m</td>
                <td class="text-end">
                  <div class="btn-group btn-group-sm">
                    <button class="btn btn-outline-primary" @click="openChapterModal(chap)">编辑内容</button>
                    <button class="btn btn-outline-danger" @click="handleDeleteChapter(chap.id)">删除</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-header bg-white d-flex justify-content-between align-items-center">
        <h5 class="mb-0">
          <i class="bi bi-list-ul me-2"></i>
          课程列表
        </h5>
        <button class="btn btn-outline-primary btn-sm" @click="loadCourses">
          <i class="bi bi-arrow-clockwise me-1"></i>
          刷新
        </button>
      </div>
      <div class="card-body">
        <div v-if="loading" class="text-center py-4">
          <div class="spinner-border text-primary"></div>
        </div>
        <div v-else-if="courseStore.courses.length === 0" class="text-center py-4 text-muted">
          暂无课程
        </div>
        <div v-else class="table-responsive">
          <table class="table align-middle">
            <thead>
              <tr>
                <th>名称</th>
                <th>难度</th>
                <th>讲师</th>
                <th>时长</th>
                <th>状态</th>
                <th class="text-end">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="course in (courseStore.courses as any[])" :key="course.id">
                <td>{{ course.title || course.name }}</td>
                <td>{{ course.difficultyLevel || '初级' }}</td>
                <td>{{ course.instructor || '-' }}</td>
                <td>{{ course.estimatedHours || 0 }} 小时</td>
                <td>
                  <span class="badge" :class="course.published ? 'bg-success' : 'bg-secondary'">
                    {{ course.published ? '已发布' : '未发布' }}
                  </span>
                </td>
                <td class="text-end">
                  <div class="btn-group btn-group-sm">
                    <button class="btn btn-outline-primary" @click="startEdit(course)">编辑</button>
                    <button class="btn btn-outline-warning" @click="handleTogglePublish(course)">
                      {{ course.published ? '取消发布' : '发布' }}
                    </button>
                    <button class="btn btn-outline-danger" @click="handleDelete(course.id)">
                      删除
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Chapter Modal -->
    <div v-if="chapterModalOpen" class="modal-overlay">
      <div class="modal-container">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h5 class="mb-0">{{ editingChapter?.id ? '编辑章节' : '创建章节' }}</h5>
          <button type="button" class="btn-close" @click="chapterModalOpen = false"></button>
        </div>
        <form @submit.prevent="handleChapterSubmit">
          <div class="mb-3">
            <label class="form-label">章节标题</label>
            <input v-model="chapterForm.title" type="text" class="form-control" required />
          </div>
          <div class="mb-3">
            <label class="form-label">章节描述</label>
            <textarea v-model="chapterForm.description" class="form-control" rows="2"></textarea>
          </div>
          <div class="mb-3">
            <label class="form-label">章节类型</label>
            <select v-model="chapterForm.type" class="form-select">
              <option value="TEXT">文本</option>
              <option value="VIDEO">视频</option>
            </select>
          </div>
          <div class="mb-3" v-if="chapterForm.type === 'TEXT'">
            <label class="form-label">章节内容</label>
            <textarea v-model="chapterForm.content" class="form-control" rows="5"></textarea>
          </div>
          <div class="mb-3" v-if="chapterForm.type === 'VIDEO'">
            <label class="form-label">视频URL</label>
            <input v-model="chapterForm.videoUrl" type="url" class="form-control" />
          </div>
          <div class="row g-3 mb-3">
            <div class="col-md-6">
              <label class="form-label">预计时长（分钟）</label>
              <input v-model.number="chapterForm.estimatedMinutes" type="number" min="1" class="form-control" />
            </div>
            <div class="col-md-6">
              <label class="form-label">排序索引</label>
              <input v-model.number="chapterForm.orderIndex" type="number" min="1" class="form-control" />
            </div>
          </div>
          <div class="d-flex justify-content-end gap-2">
            <button type="button" class="btn btn-outline-secondary" @click="chapterModalOpen = false">取消</button>
            <button type="submit" class="btn btn-primary">保存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useCourseStore } from '@/stores/course'
import { chapterApi } from '@/services/api/chapter'
import type { Course, CoursePayload, Chapter } from '@/types'

const courseStore = useCourseStore()
const loading = ref(false)
const submitting = ref(false)
const editingId = ref<string | null>(null)

const form = reactive<CoursePayload>({
  name: '',
  description: '',
  instructor: '',
  difficultyLevel: 'BEGINNER',
  estimatedHours: 0,
  tags: '',
  published: false
})

const chapters = ref<Chapter[]>([])
const loadingChapters = ref(false)
const chapterModalOpen = ref(false)
const editingChapter = ref<Partial<Chapter> | null>(null)

const chapterForm = reactive({
  title: '',
  description: '',
  content: '',
  type: 'TEXT' as 'TEXT' | 'VIDEO',
  videoUrl: '',
  estimatedMinutes: 30,
  orderIndex: 1
})

const message = reactive<{ text: string; type: string }>({
  text: '',
  type: 'alert-info'
})

const showMessage = (text: string, type = 'alert-info') => {
  message.text = text
  message.type = type
  setTimeout(() => {
    message.text = ''
  }, 3000)
}

const resetForm = () => {
  editingId.value = null
  chapters.value = []
  form.name = ''
  form.description = ''
  form.instructor = ''
  form.difficultyLevel = 'BEGINNER'
  form.estimatedHours = 0
  form.tags = ''
  form.published = false
}

const loadCourses = async () => {
  loading.value = true
  const result = await courseStore.fetchAllCourses()
  if (!result?.success) {
    showMessage(result?.message || '获取课程失败', 'alert-danger')
  }
  loading.value = false
}

const startEdit = async (course: any) => {
  const c = course as Course
  editingId.value = c.id
  form.name = c.title || c.name || ''
  form.description = c.description || ''
  form.instructor = c.instructor || ''
  form.difficultyLevel = (c.difficultyLevel as any) || 'BEGINNER'
  form.estimatedHours = c.estimatedHours || 0
  form.tags = c.tags || ''
  form.published = c.published
  
  await loadChapters(c.id)
}

const loadChapters = async (courseId: string) => {
  loadingChapters.value = true
  const result = await courseStore.fetchCourseChapters(courseId)
  if (result?.success) {
    chapters.value = result.data || []
  }
  loadingChapters.value = false
}

const openChapterModal = (chapter?: Chapter) => {
  if (chapter) {
    const chap = chapter as Chapter
    editingChapter.value = chap
    chapterForm.title = chap.title
    chapterForm.description = chap.description
    chapterForm.content = chap.content || ''
    chapterForm.type = chap.type === 'VIDEO' ? 'VIDEO' : 'TEXT'
    chapterForm.videoUrl = chap.videoUrl || ''
    chapterForm.estimatedMinutes = chap.estimatedMinutes
    chapterForm.orderIndex = chap.orderIndex
  } else {
    editingChapter.value = null
    chapterForm.title = ''
    chapterForm.description = ''
    chapterForm.content = ''
    chapterForm.type = 'TEXT'
    chapterForm.videoUrl = ''
    chapterForm.estimatedMinutes = 30
    chapterForm.orderIndex = chapters.value.length + 1
  }
  chapterModalOpen.value = true
}

const handleChapterSubmit = async () => {
  const cid = editingId.value
  if (!cid) return
  
  const payload = {
    courseId: cid,
    ...chapterForm
  }
  
  let result
  const editChapId = editingChapter.value?.id
  if (editChapId) {
    result = await chapterApi.updateChapter(editChapId, payload as any)
  } else {
    result = await chapterApi.createChapter(cid as string, payload as any)
  }

  if (result?.success) {
    showMessage('章节已保存', 'alert-success')
    chapterModalOpen.value = false
    await loadChapters(cid)
  } else {
    showMessage(result?.message || '保存章节失败', 'alert-danger')
  }
}

const handleDeleteChapter = async (id: string) => {
  if (!window.confirm('确定删除该章节？')) return
  const result = await chapterApi.deleteChapter(id)
  const cid = editingId.value
  if (result?.success && cid) {
    await loadChapters(cid)
  }
}

const handleSubmit = async () => {
  submitting.value = true
  const payload: CoursePayload = {
    name: form.name,
    description: form.description,
    instructor: form.instructor,
    difficultyLevel: form.difficultyLevel,
    estimatedHours: form.estimatedHours,
    tags: form.tags,
    published: form.published
  }

  let result;
  const cid = editingId.value;
  if (cid) {
    result = await courseStore.updateCourse(cid as string, payload)
  } else {
    result = await courseStore.createCourse(payload)
  }

  if (result?.success) {
    showMessage(editingId.value ? '课程已更新' : '课程已创建', 'alert-success')
    resetForm()
    await loadCourses()
  } else {
    showMessage(result?.message || '操作失败', 'alert-danger')
  }
  submitting.value = false
}

const handleTogglePublish = async (course: any) => {
  const c = course as Course
  const result = await courseStore.togglePublish(c.id)
  if (result?.success) {
    await loadCourses()
  } else {
    showMessage(result?.message || '更新状态失败', 'alert-danger')
  }
}

const handleDelete = async (id: string) => {
  const confirmed = window.confirm('确定要删除该课程吗？此操作不可恢复。')
  if (!confirmed) return

  const result = await courseStore.deleteCourse(id)
  if (result?.success) {
    showMessage('课程已删除', 'alert-success')
    await loadCourses()
  } else {
    showMessage(result?.message || '删除失败', 'alert-danger')
  }
}

onMounted(() => {
  loadCourses()
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1050;
}
.modal-container {
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}
.extra-small {
  font-size: 0.75rem;
}
</style>

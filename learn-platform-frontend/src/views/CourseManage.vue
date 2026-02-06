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
              <tr v-for="course in courseStore.courses" :key="course.id">
                <td>{{ course.name || course.title }}</td>
                <td>{{ course.difficulty || '初级' }}</td>
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
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useCourseStore } from '@/stores/course'
import type { Course, CoursePayload } from '@/types'

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

const startEdit = (course: Course) => {
  editingId.value = course.id
  form.name = course.name || course.title
  form.description = course.description || ''
  form.instructor = course.instructor || ''
  form.difficultyLevel = course.difficultyLevel || 'BEGINNER'
  form.estimatedHours = course.estimatedHours || 0
  form.tags = course.tags || ''
  form.published = course.published
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

  const result = editingId.value
    ? await courseStore.updateCourse(editingId.value, payload)
    : await courseStore.createCourse(payload)

  if (result?.success) {
    showMessage(editingId.value ? '课程已更新' : '课程已创建', 'alert-success')
    resetForm()
    await loadCourses()
  } else {
    showMessage(result?.message || '操作失败', 'alert-danger')
  }
  submitting.value = false
}

const handleTogglePublish = async (course: Course) => {
  const result = await courseStore.togglePublish(course.id)
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

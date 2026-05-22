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

    <!-- Concept & Relationship Management -->
    <div v-if="editingId" class="card mb-4 border-success">
      <div class="card-header bg-success-subtle">
        <ul class="nav nav-tabs card-header-tabs" role="tablist">
          <li class="nav-item">
            <button
              class="nav-link"
              :class="{ active: activeManageTab === 'concepts' }"
              type="button"
              @click="activeManageTab = 'concepts'"
            >
              <i class="bi bi-diagram-3 me-1"></i>知识点管理
            </button>
          </li>
          <li class="nav-item">
            <button
              class="nav-link"
              :class="{ active: activeManageTab === 'relations' }"
              type="button"
              @click="activeManageTab = 'relations'"
            >
              <i class="bi bi-arrow-left-right me-1"></i>关系管理
            </button>
          </li>
        </ul>
      </div>
      <div class="card-body tab-content">
        <!-- Concepts Tab -->
        <div v-show="activeManageTab === 'concepts'" class="tab-pane show active">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <span class="text-muted small">共 {{ allConcepts.length }} 个知识点</span>
            <button class="btn btn-success btn-sm" @click="openConceptModal()">
              <i class="bi bi-plus-lg me-1"></i>添加知识点
            </button>
          </div>
          <div v-if="loadingConcepts" class="text-center py-3">
            <div class="spinner-border spinner-border-sm text-success"></div>
          </div>
          <div v-else-if="allConcepts.length === 0" class="text-center py-3 text-muted small">
            暂无知识点，请添加
          </div>
          <div v-else class="table-responsive">
            <table class="table table-sm align-middle mb-0">
              <thead class="table-light">
                <tr>
                  <th>名称</th>
                  <th>所属章节</th>
                  <th>内容摘要</th>
                  <th>难度</th>
                  <th>重要度</th>
                  <th class="text-end">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="c in allConcepts" :key="c.id">
                  <td class="fw-bold">{{ c.name }}</td>
                  <td><span class="badge bg-light text-dark">{{ getChapterTitle(c.chapterId || c.chapter?.id) }}</span></td>
                  <td class="text-muted small concept-summary-cell">
                    {{ c.summary || c.description || '暂无内容' }}
                  </td>
                  <td>
                    <span class="badge" :class="c.difficultyLevel <= 2 ? 'bg-success' : c.difficultyLevel <= 4 ? 'bg-warning text-dark' : 'bg-danger'">
                      Lv.{{ c.difficultyLevel }}
                    </span>
                  </td>
                  <td>{{ c.importanceWeight }}/100</td>
                  <td class="text-end">
                    <div class="btn-group btn-group-sm">
                      <button class="btn btn-outline-primary" @click="openConceptModal(c)">编辑</button>
                      <button class="btn btn-outline-danger" @click="handleDeleteConcept(c.id)">删除</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <!-- Relations Tab -->
        <div v-show="activeManageTab === 'relations'" class="tab-pane show active">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <span class="text-muted small">共 {{ allRelationships.length }} 条关系</span>
            <button class="btn btn-success btn-sm" @click="openRelationModal()">
              <i class="bi bi-plus-lg me-1"></i>添加关系
            </button>
          </div>
          <div v-if="loadingRels" class="text-center py-3">
            <div class="spinner-border spinner-border-sm text-success"></div>
          </div>
          <div v-else-if="allRelationships.length === 0" class="text-center py-3 text-muted small">
            暂无关系，请先创建知识点再添加关系
          </div>
          <div v-else class="table-responsive">
            <table class="table table-sm align-middle mb-0">
              <thead class="table-light">
                <tr>
                  <th>源知识点</th>
                  <th>关系类型</th>
                  <th>目标知识点</th>
                  <th>权重</th>
                  <th class="text-end">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="r in allRelationships" :key="r.id">
                  <td>{{ getConceptName(r.fromConcept?.id || r.fromConceptId) }}</td>
                  <td><span class="badge" :class="relTypeBadge(r.type || r.relationshipType)">{{ relTypeLabel(r.type || r.relationshipType) }}</span></td>
                  <td>{{ getConceptName(r.toConcept?.id || r.toConceptId) }}</td>
                  <td>{{ r.weight }}</td>
                  <td class="text-end">
                    <button class="btn btn-sm btn-outline-danger" @click="handleDeleteRelation(r.id)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- Concept Modal -->
    <div v-if="conceptModalOpen" class="modal-overlay">
      <div class="modal-container modal-container-wide">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h5 class="mb-0">{{ editingConcept?.id ? '编辑知识点' : '添加知识点' }}</h5>
          <button type="button" class="btn-close" @click="conceptModalOpen = false"></button>
        </div>
        <form @submit.prevent="handleConceptSubmit">
          <div class="section-card mb-3">
            <div class="section-card-title">基础信息</div>
            <div class="row g-3">
              <div class="col-md-6">
                <label class="form-label">知识点名称</label>
                <input v-model="conceptForm.name" type="text" class="form-control" required />
              </div>
              <div class="col-md-6">
                <label class="form-label">所属章节</label>
                <select v-model="conceptForm.chapterId" class="form-select" required>
                  <option value="">-- 选择章节 --</option>
                  <option v-for="ch in chapters" :key="ch.id" :value="ch.id">{{ ch.title }} (序号: {{ ch.orderIndex }})</option>
                </select>
              </div>
              <div class="col-12">
                <label class="form-label">一句话定义</label>
                <input
                  v-model="conceptForm.summary"
                  type="text"
                  class="form-control"
                  placeholder="用一句话说明这个知识点是什么、解决什么问题"
                />
              </div>
              <div class="col-12">
                <label class="form-label">补充描述</label>
                <textarea
                  v-model="conceptForm.description"
                  class="form-control"
                  rows="2"
                  placeholder="可选，用于列表和搜索中的简短说明"
                ></textarea>
              </div>
            </div>
          </div>

          <div class="section-card mb-3">
            <div class="section-card-title">知识点内容卡片</div>
            <div class="row g-3">
              <div class="col-12">
                <label class="form-label">详细讲解</label>
                <textarea
                  v-model="conceptForm.content"
                  class="form-control"
                  rows="5"
                  placeholder="这里写清楚这个知识点的核心概念、原理、使用场景"
                ></textarea>
              </div>
              <div class="col-md-6">
                <label class="form-label">示例</label>
                <textarea
                  v-model="conceptForm.example"
                  class="form-control"
                  rows="4"
                  placeholder="例如：代码片段、公式、业务例子"
                ></textarea>
              </div>
              <div class="col-md-6">
                <label class="form-label">常见误区</label>
                <textarea
                  v-model="conceptForm.commonPitfall"
                  class="form-control"
                  rows="4"
                  placeholder="例如：常犯错误、容易混淆的点、边界条件"
                ></textarea>
              </div>
            </div>
          </div>

          <div class="row g-3 mb-3">
            <div class="col-md-6">
              <label class="form-label">难度 (1-5)</label>
              <input v-model.number="conceptForm.difficultyLevel" type="number" min="1" max="5" class="form-control" />
            </div>
            <div class="col-md-6">
              <label class="form-label">重要度 (0-100)</label>
              <input v-model.number="conceptForm.importanceWeight" type="number" min="0" max="100" class="form-control" />
            </div>
          </div>
          <div class="d-flex justify-content-end gap-2">
            <button type="button" class="btn btn-outline-secondary" @click="conceptModalOpen = false">取消</button>
            <button type="submit" class="btn btn-success">保存</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Relation Modal -->
    <div v-if="relationModalOpen" class="modal-overlay">
      <div class="modal-container">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h5 class="mb-0">添加知识点关系</h5>
          <button type="button" class="btn-close" @click="relationModalOpen = false"></button>
        </div>
        <div v-if="relationError" class="alert alert-danger py-2 small" role="alert">
          {{ relationError }}
        </div>
        <form @submit.prevent="handleRelationSubmit">
          <div class="mb-3">
            <label class="form-label">源知识点</label>
            <select v-model="relationForm.fromConceptId" class="form-select" required>
              <option value="">-- 选择 --</option>
              <option v-for="c in allConcepts" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-label">关系类型</label>
            <select v-model="relationForm.type" class="form-select" required>
              <option value="PREREQUISITE">先修关系</option>
              <option value="DEPENDS_ON">依赖关系</option>
              <option value="USES">使用关系</option>
              <option value="SIMILAR_TO">相似关系</option>
              <option value="PART_OF">包含关系</option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-label">目标知识点</label>
            <select v-model="relationForm.toConceptId" class="form-select" required>
              <option value="">-- 选择 --</option>
              <option v-for="c in allConcepts" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
          <div class="row g-3 mb-3">
            <div class="col-md-6">
              <label class="form-label">权重 (0-1)</label>
              <input v-model.number="relationForm.weight" type="number" min="0" max="1" step="0.1" class="form-control" />
            </div>
            <div class="col-md-6">
              <label class="form-label">描述</label>
              <input v-model="relationForm.description" type="text" class="form-control" placeholder="可选" />
            </div>
          </div>
          <div class="d-flex justify-content-end gap-2">
            <button type="button" class="btn btn-outline-secondary" @click="relationModalOpen = false">取消</button>
            <button type="submit" class="btn btn-success" :disabled="relationSubmitting">
              <span v-if="relationSubmitting" class="spinner-border spinner-border-sm me-2"></span>
              创建关系
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
import http from '@/services/http'
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

// Concept state
const allConcepts = ref<any[]>([])
const loadingConcepts = ref(false)
const conceptModalOpen = ref(false)
const editingConcept = ref<any | null>(null)
const conceptForm = reactive({
  name: '',
  description: '',
  summary: '',
  content: '',
  example: '',
  commonPitfall: '',
  chapterId: '',
  difficultyLevel: 1,
  importanceWeight: 50,
  courseId: ''
})
const activeManageTab = ref<'concepts' | 'relations'>('concepts')

// Relation state
const allRelationships = ref<any[]>([])
const loadingRels = ref(false)
const relationModalOpen = ref(false)
const relationForm = reactive({ fromConceptId: '', toConceptId: '', type: 'PREREQUISITE', weight: 0.8, description: '' })
const relationSubmitting = ref(false)
const relationError = ref('')

async function loadConceptsAndRels(courseId: string) {
  loadingConcepts.value = true
  loadingRels.value = true
  try {
    const [cRes, rRes] = await Promise.all([
      http.get(`/courses/${courseId}/concepts`),
      http.get(`/courses/${courseId}/relationships`)
    ])
    if ((cRes as any).success) allConcepts.value = (cRes as any).data || []
    if ((rRes as any).success) allRelationships.value = (rRes as any).data || []
  } catch (e) { console.error(e) }
  loadingConcepts.value = false
  loadingRels.value = false
}

function getChapterTitle(chapterId: string): string {
  const ch = chapters.value.find((c: any) => c.id === chapterId)
  return ch?.title || chapterId?.slice(-6) || '-'
}

function getConceptName(conceptId: string): string {
  if (!conceptId) return '-'
  const c = allConcepts.value.find((x: any) => x.id === conceptId)
  return c?.name || conceptId.slice(-6)
}

function openConceptModal(concept?: any) {
  if (concept) {
    editingConcept.value = concept
    conceptForm.name = concept.name
    conceptForm.description = concept.description || ''
    conceptForm.summary = concept.summary || ''
    conceptForm.content = concept.content || ''
    conceptForm.example = concept.example || ''
    conceptForm.commonPitfall = concept.commonPitfall || ''
    conceptForm.chapterId = concept.chapter?.id || concept.chapterId || ''
    conceptForm.difficultyLevel = concept.difficultyLevel || 1
    conceptForm.importanceWeight = concept.importanceWeight || 50
  } else {
    editingConcept.value = null
    conceptForm.name = ''
    conceptForm.description = ''
    conceptForm.summary = ''
    conceptForm.content = ''
    conceptForm.example = ''
    conceptForm.commonPitfall = ''
    conceptForm.chapterId = ''
    conceptForm.difficultyLevel = 1
    conceptForm.importanceWeight = 50
  }
  conceptForm.courseId = editingId.value || ''
  conceptModalOpen.value = true
}

async function handleConceptSubmit() {
  if (!editingId.value) return
  try {
    let res
    if (editingConcept.value?.id) {
      res = await http.put(`/concepts/${editingConcept.value.id}`, {
        name: conceptForm.name,
        description: conceptForm.description,
        summary: conceptForm.summary,
        content: conceptForm.content,
        example: conceptForm.example,
        commonPitfall: conceptForm.commonPitfall,
        difficultyLevel: conceptForm.difficultyLevel,
        importanceWeight: conceptForm.importanceWeight
      })
    } else {
      res = await http.post('/concepts', {
        ...conceptForm, courseId: editingId.value
      })
    }
    if ((res as any).success) {
      showMessage('知识点已保存', 'alert-success')
      conceptModalOpen.value = false
      await loadConceptsAndRels(editingId.value)
    } else {
      showMessage((res as any).message || '保存失败', 'alert-danger')
    }
  } catch (e) { console.error(e) }
}

async function handleDeleteConcept(id: string) {
  if (!window.confirm('确定删除该知识点？相关关系也会受影响。')) return
  const res = await http.delete(`/concepts/${id}`)
  if ((res as any).success && editingId.value) {
    await loadConceptsAndRels(editingId.value)
  }
}

function openRelationModal() {
  relationForm.fromConceptId = ''
  relationForm.toConceptId = ''
  relationForm.type = 'PREREQUISITE'
  relationForm.weight = 0.8
  relationForm.description = ''
  relationError.value = ''
  relationModalOpen.value = true
}

async function handleRelationSubmit() {
  relationError.value = ''

  if (!relationForm.fromConceptId || !relationForm.toConceptId) {
    relationError.value = '请选择源知识点和目标知识点'
    return
  }

  if (relationForm.fromConceptId === relationForm.toConceptId) {
    relationError.value = '源知识点和目标知识点不能是同一个'
    return
  }

  const duplicateRelation = allRelationships.value.find((item: any) => {
    const fromId = item.fromConcept?.id || item.fromConceptId
    const toId = item.toConcept?.id || item.toConceptId
    const type = item.type || item.relationshipType
    return fromId === relationForm.fromConceptId
      && toId === relationForm.toConceptId
      && type === relationForm.type
  })

  if (duplicateRelation) {
    relationError.value = '这条关系已经存在了，请不要重复创建'
    return
  }

  relationSubmitting.value = true
  try {
    const res = await http.post('/relationships', { ...relationForm })
    if ((res as any).success) {
      showMessage('关系已创建', 'alert-success')
      relationModalOpen.value = false
      if (editingId.value) await loadConceptsAndRels(editingId.value)
    } else {
      relationError.value = (res as any).message || '创建失败'
      showMessage((res as any).message || '创建失败', 'alert-danger')
    }
  } catch (e: any) {
    relationError.value = e?.message || e?.error || '创建关系失败，请稍后重试'
    console.error(e)
  } finally {
    relationSubmitting.value = false
  }
}

async function handleDeleteRelation(id: string) {
  if (!window.confirm('确定删除该关系？')) return
  const res = await http.delete(`/relationships/${id}`)
  if ((res as any).success && editingId.value) {
    await loadConceptsAndRels(editingId.value)
  }
}

function relTypeLabel(type: string): string {
  const map: Record<string, string> = { PREREQUISITE: '先修', DEPENDS_ON: '依赖', USES: '使用', SIMILAR_TO: '相似', PART_OF: '包含' }
  return map[type] || type
}

function relTypeBadge(type: string): string {
  const map: Record<string, string> = { PREREQUISITE: 'bg-primary', DEPENDS_ON: 'bg-purple', USES: 'bg-warning text-dark', SIMILAR_TO: 'bg-info', PART_OF: 'bg-secondary' }
  return map[type] || 'bg-light text-dark'
}

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
  allConcepts.value = []
  allRelationships.value = []
  activeManageTab.value = 'concepts'
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
  activeManageTab.value = 'concepts'
  form.name = c.title || c.name || ''
  form.description = c.description || ''
  form.instructor = c.instructor || ''
  form.difficultyLevel = (c.difficultyLevel as any) || 'BEGINNER'
  form.estimatedHours = c.estimatedHours || 0
  form.tags = c.tags || ''
  form.published = c.published
  
  await Promise.all([
    loadChapters(c.id),
    loadConceptsAndRels(c.id)
  ])
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
    await Promise.all([
      loadChapters(cid),
      loadConceptsAndRels(cid)
    ])
  } else {
    showMessage(result?.message || '保存章节失败', 'alert-danger')
  }
}

const handleDeleteChapter = async (id: string) => {
  if (!window.confirm('确定删除该章节？')) return
  const result = await chapterApi.deleteChapter(id)
  const cid = editingId.value
  if (result?.success && cid) {
    await Promise.all([
      loadChapters(cid),
      loadConceptsAndRels(cid)
    ])
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
    if (editingId.value === id) {
      resetForm()
    }
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

.modal-container-wide {
  max-width: 760px;
}

.section-card {
  padding: 1rem;
  border-radius: 0.9rem;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.section-card-title {
  font-size: 0.85rem;
  font-weight: 700;
  color: #334155;
  margin-bottom: 0.85rem;
}

.concept-summary-cell {
  min-width: 240px;
  max-width: 320px;
  line-height: 1.4;
}
</style>

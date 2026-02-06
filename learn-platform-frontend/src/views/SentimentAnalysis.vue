<template>
  <div class="container">
    <div class="row mb-4">
      <div class="col-12">
        <h1 class="display-6">
          <i class="bi bi-emoji-smile me-2"></i>
          市场情绪分析
        </h1>
        <p class="text-muted">生成市场情绪报告并查看历史记录</p>
      </div>
    </div>

    <div v-if="message" class="alert alert-warning" role="alert">
      <i class="bi bi-info-circle me-2"></i>
      {{ message }}
    </div>

    <div class="card mb-4">
      <div class="card-body d-flex flex-wrap gap-2">
        <button class="btn btn-primary" @click="runAnalysis">
          <i class="bi bi-play-circle me-1"></i>
          生成情绪报告
        </button>
        <button class="btn btn-outline-secondary" @click="loadReports">
          <i class="bi bi-arrow-clockwise me-1"></i>
          刷新列表
        </button>
      </div>
    </div>

    <div class="row g-4">
      <div class="col-lg-4">
        <div class="card h-100">
          <div class="card-header bg-white">
            <h5 class="mb-0">
              <i class="bi bi-journal-text me-2"></i>
              报告列表
            </h5>
          </div>
          <div class="card-body">
            <div v-if="reports.length === 0" class="text-center text-muted">暂无报告</div>
            <ul v-else class="list-group list-group-flush">
              <li v-for="report in reports" :key="report.filename" class="list-group-item">
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <div class="fw-semibold">{{ report.filename }}</div>
                    <small class="text-muted">{{ report.date }}</small>
                  </div>
                  <button class="btn btn-outline-danger btn-sm" @click="deleteReport(report.filename)">
                    删除
                  </button>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <div class="col-lg-8">
        <div class="card h-100">
          <div class="card-header bg-white">
            <h5 class="mb-0">
              <i class="bi bi-file-text me-2"></i>
              最新报告内容
            </h5>
          </div>
          <div class="card-body">
            <pre class="bg-light p-3 rounded">{{ reportContent }}</pre>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { sentimentApi } from '@/services/api/sentiment'

const reports = ref<any[]>([])
const reportContent = ref('暂无报告内容')
const message = ref('')

const loadReports = async () => {
  message.value = ''
  const response = await sentimentApi.listReports()
  if (response.success) {
    reports.value = Array.isArray(response.data) ? response.data : []
  } else {
    reports.value = []
    message.value = response.message || '获取报告失败'
  }
}

const runAnalysis = async () => {
  message.value = ''
  const response = await sentimentApi.analyze()
  if (response.success) {
    const data = response.data as any
    reportContent.value = data?.report || '暂无报告内容'
    await loadReports()
  } else {
    message.value = response.message || '情绪分析失败'
  }
}

const deleteReport = async (filename: string) => {
  if (!window.confirm('确定要删除该报告吗？')) {
    return
  }
  const response = await sentimentApi.deleteReport(filename)
  if (response.success) {
    await loadReports()
  } else {
    message.value = response.message || '删除报告失败'
  }
}

onMounted(() => {
  loadReports()
})
</script>

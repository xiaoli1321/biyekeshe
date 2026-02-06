<template>
  <div class="container">
    <div class="row mb-4">
      <div class="col-12">
        <h1 class="display-6">
          <i class="bi bi-newspaper me-2"></i>
          新闻资讯
        </h1>
        <p class="text-muted">查看个性化新闻流与热门资讯</p>
      </div>
    </div>

    <div v-if="message" class="alert alert-warning" role="alert">
      <i class="bi bi-info-circle me-2"></i>
      {{ message }}
    </div>

    <div class="card mb-4">
      <div class="card-body">
        <div class="row g-3 align-items-end">
          <div class="col-md-3">
            <label class="form-label">分类</label>
            <select v-model="category" class="form-select">
              <option value="all">全部</option>
              <option value="flash">自选股快讯</option>
              <option value="fund">自选基金</option>
              <option value="announcement">公告</option>
              <option value="research">研报</option>
              <option value="hot">热门</option>
            </select>
          </div>
          <div class="col-md-2">
            <label class="form-label">页码</label>
            <input v-model.number="page" type="number" min="1" class="form-control" />
          </div>
          <div class="col-md-2">
            <label class="form-label">每页数量</label>
            <input v-model.number="pageSize" type="number" min="1" class="form-control" />
          </div>
          <div class="col-md-2">
            <button class="btn btn-primary w-100" @click="loadNews">加载新闻</button>
          </div>
          <div class="col-md-3">
            <button class="btn btn-outline-secondary w-100" @click="loadHotNews">热门新闻</button>
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-header bg-white">
        <h5 class="mb-0">
          <i class="bi bi-list-ul me-2"></i>
          新闻列表
        </h5>
      </div>
      <div class="card-body">
        <div v-if="newsList.length === 0" class="text-center text-muted">暂无新闻</div>
        <div v-else class="list-group">
          <div v-for="item in newsList" :key="item.id || item.news_id || item.title" class="list-group-item">
            <div class="d-flex justify-content-between">
              <div>
                <div class="fw-semibold">{{ item.title || item.news_title || '未命名' }}</div>
                <small class="text-muted">{{ item.source || item.news_source || item.time || '' }}</small>
              </div>
              <span v-if="item.category" class="badge bg-primary">{{ item.category }}</span>
            </div>
            <p class="mb-0 text-muted small mt-2">{{ item.summary || item.brief || '' }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { newsApi } from '@/services/api/news'

const category = ref('all')
const page = ref(1)
const pageSize = ref(20)
const newsList = ref<any[]>([])
const message = ref('')

const loadNews = async () => {
  message.value = ''
  const response = await newsApi.getNewsFeed(category.value, page.value, pageSize.value)
  if (response.success) {
    const data = response.data as any
    newsList.value = data?.news || data?.items || data?.list || []
  } else {
    newsList.value = []
    message.value = response.message || '获取新闻失败'
  }
}

const loadHotNews = async () => {
  message.value = ''
  const response = await newsApi.getHotNews(30)
  if (response.success) {
    const data = response.data as any
    newsList.value = data?.news || data?.items || data?.list || []
  } else {
    newsList.value = []
    message.value = response.message || '获取热门新闻失败'
  }
}
</script>

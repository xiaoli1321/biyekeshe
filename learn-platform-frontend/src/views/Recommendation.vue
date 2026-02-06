<template>
  <div class="container">
    <div class="row mb-4">
      <div class="col-12">
        <h1 class="display-6">
          <i class="bi bi-stars me-2"></i>
          AI 推荐
        </h1>
        <p class="text-muted">查看短期/长期股票与基金推荐</p>
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
            <label class="form-label">推荐类型</label>
            <select v-model="mode" class="form-select">
              <option value="stock-short">短期股票</option>
              <option value="stock-long">长期股票</option>
              <option value="fund-short">短期基金</option>
              <option value="fund-long">长期基金</option>
            </select>
          </div>
          <div class="col-md-2">
            <label class="form-label">数量</label>
            <input v-model.number="limit" type="number" min="1" class="form-control" />
          </div>
          <div class="col-md-2">
            <label class="form-label">最低评分</label>
            <input v-model.number="minScore" type="number" min="0" class="form-control" />
          </div>
          <div class="col-md-2">
            <button class="btn btn-primary w-100" @click="loadRecommendations">获取推荐</button>
          </div>
          <div class="col-md-3">
            <button class="btn btn-outline-secondary w-100" @click="loadLatest">最新汇总</button>
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-header bg-white">
        <h5 class="mb-0">
          <i class="bi bi-lightning-charge me-2"></i>
          推荐结果
        </h5>
      </div>
      <div class="card-body">
        <div v-if="recommendations.length === 0" class="text-center text-muted">暂无推荐数据</div>
        <div v-else class="table-responsive">
          <table class="table align-middle">
            <thead>
              <tr>
                <th>名称</th>
                <th>代码</th>
                <th>评分</th>
                <th>理由</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in recommendations" :key="item.code || item.symbol || item.name">
                <td>{{ item.name || item.fund_name || item.stock_name || '-' }}</td>
                <td>{{ item.code || item.fund_code || item.stock_code || '-' }}</td>
                <td>{{ item.recommendation_score || item.score || '-' }}</td>
                <td>{{ item.reason || item.summary || '-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-if="marketView" class="alert alert-info mt-3">
          <i class="bi bi-chat-left-text me-2"></i>
          市场观点：{{ marketView }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { recommendApi } from '@/services/api/recommend'

const mode = ref('stock-short')
const limit = ref(10)
const minScore = ref(60)
const recommendations = ref<any[]>([])
const marketView = ref('')
const message = ref('')

const loadRecommendations = async () => {
  message.value = ''
  marketView.value = ''

  let response
  if (mode.value === 'stock-short') {
    response = await recommendApi.getShortStock(limit.value, minScore.value)
  } else if (mode.value === 'stock-long') {
    response = await recommendApi.getLongStock(limit.value, minScore.value)
  } else if (mode.value === 'fund-short') {
    response = await recommendApi.getShortFund(limit.value, minScore.value)
  } else {
    response = await recommendApi.getLongFund(limit.value, minScore.value)
  }

  if (response.success) {
    const data = response.data as any
    recommendations.value = Array.isArray(data?.recommendations) ? data.recommendations : []
    marketView.value = data?.market_view || ''
  } else {
    recommendations.value = []
    message.value = response.message || '获取推荐失败'
  }
}

const loadLatest = async () => {
  message.value = ''
  const response = await recommendApi.getLatest()
  if (response.success) {
    const data = response.data as any
    recommendations.value = Array.isArray(data?.recommendations) ? data.recommendations : []
    marketView.value = data?.market_view || ''
  } else {
    recommendations.value = []
    message.value = response.message || '获取推荐失败'
  }
}
</script>

<template>
  <div class="container">
    <div class="row mb-4">
      <div class="col-12">
        <h1 class="display-6">
          <i class="bi bi-activity me-2"></i>
          股票监控
        </h1>
        <p class="text-muted">搜索股票并查看行情、历史与AI诊断</p>
      </div>
    </div>

    <div v-if="message" class="alert alert-warning" role="alert">
      <i class="bi bi-info-circle me-2"></i>
      {{ message }}
    </div>

    <div class="card mb-4">
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label">股票代码/名称</label>
            <input v-model="query" type="text" class="form-control" placeholder="例如：600519" />
          </div>
          <div class="col-md-2 d-flex align-items-end">
            <button class="btn btn-primary w-100" @click="handleSearch">搜索</button>
          </div>
        </div>
        <div class="table-responsive mt-3">
          <table class="table align-middle">
            <thead>
              <tr>
                <th>代码</th>
                <th>名称</th>
                <th class="text-end">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="stocks.length === 0">
                <td colspan="3" class="text-center text-muted">暂无结果</td>
              </tr>
              <tr v-for="stock in stocks" :key="stock.code">
                <td>{{ stock.code }}</td>
                <td>{{ stock.name }}</td>
                <td class="text-end">
                  <button class="btn btn-outline-primary btn-sm" @click="selectStock(stock)">
                    查看监控
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div v-if="selectedStock" class="row g-4">
      <div class="col-lg-4">
        <div class="card h-100">
          <div class="card-header bg-white">
            <h5 class="mb-0">
              <i class="bi bi-info-circle me-2"></i>
              股票概览
            </h5>
          </div>
          <div class="card-body">
            <div class="mb-2"><strong>代码：</strong>{{ selectedStock.code }}</div>
            <div class="mb-2"><strong>名称：</strong>{{ selectedStock.name }}</div>
            <div v-if="stockDetails?.quote">
              <div class="mb-2"><strong>最新价：</strong>{{ stockDetails.quote.price }}</div>
              <div class="mb-2"><strong>涨跌幅：</strong>{{ stockDetails.quote.change_pct }}</div>
              <div class="mb-2"><strong>成交量：</strong>{{ stockDetails.quote.volume }}</div>
            </div>
            <div v-if="stockDetails?.info">
              <div class="mb-2"><strong>行业：</strong>{{ stockDetails.info.industry }}</div>
              <div class="mb-2"><strong>市值：</strong>{{ stockDetails.info.market_cap }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-lg-8">
        <div class="card mb-4">
          <div class="card-header bg-white">
            <h5 class="mb-0">
              <i class="bi bi-graph-up me-2"></i>
              历史行情（最近）
            </h5>
          </div>
          <div class="card-body">
            <div class="table-responsive">
              <table class="table table-sm">
                <thead>
                  <tr>
                    <th>日期</th>
                    <th>收盘价</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="stockHistory.length === 0">
                    <td colspan="2" class="text-center text-muted">暂无历史行情</td>
                  </tr>
                  <tr v-for="item in stockHistory" :key="item.date">
                    <td>{{ item.date }}</td>
                    <td>{{ item.close }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <div class="col-12">
        <div class="card">
          <div class="card-header bg-white">
            <h5 class="mb-0">
              <i class="bi bi-stars me-2"></i>
              AI诊断
            </h5>
          </div>
          <div class="card-body">
            <pre class="bg-light p-3 rounded">{{ formatJson(stockDiagnosis) }}</pre>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { stockApi } from '@/services/api/stock'

const query = ref('')
const stocks = ref<any[]>([])
const selectedStock = ref<any | null>(null)
const stockDetails = ref<any | null>(null)
const stockHistory = ref<any[]>([])
const stockDiagnosis = ref<any | null>(null)
const message = ref('')

const handleSearch = async () => {
  message.value = ''
  const response = await stockApi.searchMarketStocks(query.value.trim())
  if (response.success) {
    stocks.value = Array.isArray(response.data) ? response.data : []
  } else {
    stocks.value = []
    message.value = response.message || '搜索失败'
  }
}

const selectStock = async (stock: any) => {
  selectedStock.value = stock
  message.value = ''

  const [details, history, diagnosis] = await Promise.all([
    stockApi.getStockDetails(stock.code),
    stockApi.getStockHistory(stock.code),
    stockApi.getStockAiDiagnosis(stock.code)
  ])

  stockDetails.value = details.success ? details.data : null
  stockHistory.value = history.success && Array.isArray(history.data)
    ? history.data.slice(0, 10)
    : []
  stockDiagnosis.value = diagnosis.success ? diagnosis.data : null

  if (!details.success || !history.success) {
    message.value = details.message || history.message || '获取股票数据失败'
  }
}

const formatJson = (value: any) => {
  if (!value) {
    return '暂无数据'
  }
  return JSON.stringify(value, null, 2)
}
</script>

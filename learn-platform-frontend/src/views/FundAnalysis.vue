<template>
  <div class="container">
    <div class="row mb-4">
      <div class="col-12">
        <h1 class="display-6">
          <i class="bi bi-bar-chart-line me-2"></i>
          基金分析
        </h1>
        <p class="text-muted">搜索基金并查看净值、风险指标与诊断结果</p>
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
            <label class="form-label">基金代码/名称</label>
            <input v-model="query" type="text" class="form-control" placeholder="例如：005963" />
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
              <tr v-if="funds.length === 0">
                <td colspan="3" class="text-center text-muted">暂无结果</td>
              </tr>
              <tr v-for="fund in funds" :key="fund.code">
                <td>{{ fund.code }}</td>
                <td>{{ fund.name }}</td>
                <td class="text-end">
                  <button class="btn btn-outline-primary btn-sm" @click="selectFund(fund)">
                    查看分析
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div v-if="selectedFund" class="row g-4">
      <div class="col-lg-4">
        <div class="card h-100">
          <div class="card-header bg-white">
            <h5 class="mb-0">
              <i class="bi bi-info-circle me-2"></i>
              基金概览
            </h5>
          </div>
          <div class="card-body">
            <div class="mb-2"><strong>代码：</strong>{{ selectedFund.code }}</div>
            <div class="mb-2"><strong>名称：</strong>{{ selectedFund.name }}</div>
            <div v-if="fundDetails?.info">
              <div class="mb-2"><strong>基金经理：</strong>{{ fundDetails.info.manager }}</div>
              <div class="mb-2"><strong>规模：</strong>{{ fundDetails.info.size }}</div>
              <div class="mb-2"><strong>类型：</strong>{{ fundDetails.info.type }}</div>
              <div class="mb-2"><strong>最新净值：</strong>{{ fundDetails.info.nav }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-lg-8">
        <div class="card mb-4">
          <div class="card-header bg-white">
            <h5 class="mb-0">
              <i class="bi bi-graph-up me-2"></i>
              基金净值走势（最近）
            </h5>
          </div>
          <div class="card-body">
            <div class="table-responsive">
              <table class="table table-sm">
                <thead>
                  <tr>
                    <th>日期</th>
                    <th>净值</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="fundNavHistory.length === 0">
                    <td colspan="2" class="text-center text-muted">暂无净值数据</td>
                  </tr>
                  <tr v-for="item in fundNavHistory" :key="item.date">
                    <td>{{ item.date }}</td>
                    <td>{{ item.value }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <div class="col-lg-6">
        <div class="card h-100">
          <div class="card-header bg-white">
            <h5 class="mb-0">
              <i class="bi bi-shield-check me-2"></i>
              风险指标
            </h5>
          </div>
          <div class="card-body">
            <pre class="bg-light p-3 rounded">{{ formatJson(fundRiskMetrics) }}</pre>
          </div>
        </div>
      </div>

      <div class="col-lg-6">
        <div class="card h-100">
          <div class="card-header bg-white">
            <h5 class="mb-0">
              <i class="bi bi-clipboard-data me-2"></i>
              基金诊断
            </h5>
          </div>
          <div class="card-body">
            <pre class="bg-light p-3 rounded">{{ formatJson(fundDiagnosis) }}</pre>
          </div>
        </div>
      </div>

      <div class="col-12">
        <div class="card">
          <div class="card-header bg-white">
            <h5 class="mb-0">
              <i class="bi bi-diagram-2 me-2"></i>
              持仓与表现
            </h5>
          </div>
          <div class="card-body">
            <div class="row g-3">
              <div class="col-md-6">
                <h6>近阶段业绩</h6>
                <ul class="list-group list-group-flush">
                  <li v-if="fundPerformance.length === 0" class="list-group-item text-muted">暂无数据</li>
                  <li v-for="item in fundPerformance" :key="item['时间范围']" class="list-group-item">
                    {{ item['时间范围'] }}：{{ item['收益率'] }}（排名 {{ item['同类排名'] }}）
                  </li>
                </ul>
              </div>
              <div class="col-md-6">
                <h6>主要持仓</h6>
                <div class="table-responsive">
                  <table class="table table-sm">
                    <thead>
                      <tr>
                        <th>名称</th>
                        <th>占比</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-if="fundHoldings.length === 0">
                        <td colspan="2" class="text-center text-muted">暂无持仓数据</td>
                      </tr>
                      <tr v-for="item in fundHoldings" :key="item['股票名称'] || item['名称'] || item['代码']">
                        <td>{{ item['股票名称'] || item['名称'] || item['代码'] }}</td>
                        <td>{{ item['占净值比例'] || item['占比'] || '-' }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { fundApi } from '@/services/api/fund'

const query = ref('')
const funds = ref<any[]>([])
const selectedFund = ref<any | null>(null)
const fundDetails = ref<any | null>(null)
const fundNavHistory = ref<any[]>([])
const fundDiagnosis = ref<any | null>(null)
const fundRiskMetrics = ref<any | null>(null)
const message = ref('')

const fundPerformance = computed(() => fundDetails.value?.performance || [])
const fundHoldings = computed(() => fundDetails.value?.portfolio || [])

const handleSearch = async () => {
  message.value = ''
  const response = await fundApi.searchMarketFunds(query.value.trim())
  if (response.success) {
    funds.value = Array.isArray(response.data) ? response.data : []
  } else {
    funds.value = []
    message.value = response.message || '搜索失败'
  }
}

const selectFund = async (fund: any) => {
  selectedFund.value = fund
  message.value = ''

  const [details, nav, diagnosis, risk] = await Promise.all([
    fundApi.getFundDetails(fund.code),
    fundApi.getFundNavHistory(fund.code),
    fundApi.getFundDiagnosis(fund.code),
    fundApi.getFundRiskMetrics(fund.code)
  ])

  fundDetails.value = details.success ? details.data : null
  fundNavHistory.value = nav.success && Array.isArray(nav.data) ? nav.data.slice(0, 10) : []
  fundDiagnosis.value = diagnosis.success ? diagnosis.data : null
  fundRiskMetrics.value = risk.success ? risk.data : null

  if (!details.success || !nav.success) {
    message.value = details.message || nav.message || '获取基金数据失败'
  }
}

const formatJson = (value: any) => {
  if (!value) {
    return '暂无数据'
  }
  return JSON.stringify(value, null, 2)
}
</script>

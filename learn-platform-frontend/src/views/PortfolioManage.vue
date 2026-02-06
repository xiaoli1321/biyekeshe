<template>
  <div class="container">
    <div class="row mb-4">
      <div class="col-12">
        <h1 class="display-6">
          <i class="bi bi-pie-chart me-2"></i>
          基金投资组合管理
        </h1>
        <p class="text-muted">管理投资组合、持仓与交易记录</p>
      </div>
    </div>

    <div v-if="message.text" class="alert" :class="message.type" role="alert">
      <i class="bi bi-info-circle me-2"></i>
      {{ message.text }}
    </div>

    <div class="row g-4">
      <div class="col-lg-4">
        <div class="card mb-4">
          <div class="card-header bg-white d-flex justify-content-between align-items-center">
            <h5 class="mb-0">
              <i class="bi bi-folder2-open me-2"></i>
              我的组合
            </h5>
            <button class="btn btn-outline-primary btn-sm" @click="reloadPortfolios">
              <i class="bi bi-arrow-clockwise me-1"></i>
              刷新
            </button>
          </div>
          <div class="card-body">
            <div v-if="portfolioStore.loading" class="text-center py-3">
              <div class="spinner-border text-primary"></div>
            </div>
            <div v-else-if="portfolioStore.portfolios.length === 0" class="text-center text-muted py-3">
              暂无组合，请先创建
            </div>
            <ul v-else class="list-group list-group-flush">
              <li
                v-for="portfolio in portfolioStore.portfolios"
                :key="portfolio.id"
                class="list-group-item d-flex justify-content-between align-items-center"
              >
                <div>
                  <div class="fw-semibold">
                    {{ portfolio.name }}
                    <span
                      v-if="isDefaultPortfolio(portfolio)"
                      class="badge bg-success ms-2"
                    >默认</span>
                  </div>
                  <small class="text-muted">基准：{{ portfolio.benchmark_code || '000300.SH' }}</small>
                </div>
                <div class="btn-group btn-group-sm">
                  <button class="btn btn-outline-primary" @click="selectPortfolio(portfolio.id)">
                    查看
                  </button>
                  <button class="btn btn-outline-secondary" @click="startEditPortfolio(portfolio)">
                    编辑
                  </button>
                  <button
                    v-if="!isDefaultPortfolio(portfolio)"
                    class="btn btn-outline-success"
                    @click="handleSetDefault(portfolio.id)"
                  >
                    设默认
                  </button>
                  <button class="btn btn-outline-danger" @click="handleDeletePortfolio(portfolio.id)">
                    删除
                  </button>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <div class="col-lg-8">
        <div class="card mb-4">
          <div class="card-header bg-white">
            <h5 class="mb-0">
              <i class="bi bi-plus-circle me-2"></i>
              {{ editingPortfolioId ? '编辑组合' : '创建组合' }}
            </h5>
          </div>
          <div class="card-body">
            <form @submit.prevent="handleSubmitPortfolio">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label">组合名称</label>
                  <input v-model="portfolioForm.name" type="text" class="form-control" required />
                </div>
                <div class="col-md-6">
                  <label class="form-label">基准指数</label>
                  <input v-model="portfolioForm.benchmarkCode" type="text" class="form-control" />
                </div>
                <div class="col-md-12">
                  <label class="form-label">组合描述</label>
                  <textarea v-model="portfolioForm.description" class="form-control" rows="2"></textarea>
                </div>
                <div class="col-md-12">
                  <div class="form-check">
                    <input
                      v-model="portfolioForm.isDefault"
                      class="form-check-input"
                      type="checkbox"
                      id="defaultPortfolio"
                    />
                    <label class="form-check-label" for="defaultPortfolio">设为默认组合</label>
                  </div>
                </div>
              </div>
              <div class="mt-3 d-flex gap-2">
                <button class="btn btn-primary" type="submit">
                  {{ editingPortfolioId ? '保存修改' : '创建组合' }}
                </button>
                <button v-if="editingPortfolioId" class="btn btn-outline-secondary" type="button" @click="resetPortfolioForm">
                  取消编辑
                </button>
              </div>
            </form>
          </div>
        </div>

        <div class="card">
          <div class="card-header bg-white">
            <h5 class="mb-0">
              <i class="bi bi-graph-up-arrow me-2"></i>
              组合概览
            </h5>
          </div>
          <div class="card-body">
            <div v-if="!activePortfolio" class="text-muted">请选择组合查看数据</div>
            <div v-else>
              <div class="row g-3">
                <div class="col-md-3">
                  <div class="p-3 border rounded">
                    <div class="text-muted small">总市值</div>
                    <div class="fw-semibold">{{ formatMoney(portfolioStore.summary?.total_value) }}</div>
                  </div>
                </div>
                <div class="col-md-3">
                  <div class="p-3 border rounded">
                    <div class="text-muted small">总成本</div>
                    <div class="fw-semibold">{{ formatMoney(portfolioStore.summary?.total_cost) }}</div>
                  </div>
                </div>
                <div class="col-md-3">
                  <div class="p-3 border rounded">
                    <div class="text-muted small">盈亏金额</div>
                    <div :class="pnlClass(portfolioStore.summary?.total_pnl)">
                      {{ formatMoney(portfolioStore.summary?.total_pnl) }}
                    </div>
                  </div>
                </div>
                <div class="col-md-3">
                  <div class="p-3 border rounded">
                    <div class="text-muted small">盈亏比例</div>
                    <div :class="pnlClass(portfolioStore.summary?.total_pnl_pct)">
                      {{ formatPercent(portfolioStore.summary?.total_pnl_pct) }}
                    </div>
                  </div>
                </div>
              </div>

              <div class="mt-3">
                <div class="text-muted small">资产配置</div>
                <div class="d-flex flex-wrap gap-2 mt-2">
                  <span
                    v-for="item in allocationByType"
                    :key="item.key"
                    class="badge bg-primary"
                  >
                    {{ item.key }}: {{ item.value.toFixed(2) }}%
                  </span>
                  <span v-if="allocationByType.length === 0" class="text-muted">暂无配置数据</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="card my-4">
      <div class="card-header bg-white">
        <h5 class="mb-0">
          <i class="bi bi-stack me-2"></i>
          持仓管理
        </h5>
      </div>
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-2">
            <label class="form-label">类型</label>
            <select v-model="positionForm.assetType" class="form-select">
              <option value="fund">基金</option>
              <option value="stock">股票</option>
            </select>
          </div>
          <div class="col-md-2">
            <label class="form-label">代码</label>
            <input v-model="positionForm.assetCode" type="text" class="form-control" />
          </div>
          <div class="col-md-3">
            <label class="form-label">名称</label>
            <input v-model="positionForm.assetName" type="text" class="form-control" />
          </div>
          <div class="col-md-2">
            <label class="form-label">份额</label>
            <input v-model.number="positionForm.totalShares" type="number" min="0" class="form-control" />
          </div>
          <div class="col-md-2">
            <label class="form-label">均价</label>
            <input v-model.number="positionForm.averageCost" type="number" min="0" class="form-control" />
          </div>
          <div class="col-md-1 d-flex align-items-end">
            <button class="btn btn-primary w-100" @click="handleCreatePosition">新增</button>
          </div>
        </div>

        <div class="row g-3 mt-2">
          <div class="col-md-3">
            <label class="form-label">行业</label>
            <input v-model="positionForm.sector" type="text" class="form-control" />
          </div>
          <div class="col-md-9">
            <label class="form-label">备注</label>
            <input v-model="positionForm.notes" type="text" class="form-control" />
          </div>
        </div>

        <div class="table-responsive mt-4">
          <table class="table align-middle">
            <thead>
              <tr>
                <th>类型</th>
                <th>代码</th>
                <th>名称</th>
                <th>份额</th>
                <th>均价</th>
                <th>现价</th>
                <th>市值</th>
                <th>盈亏%</th>
                <th class="text-end">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="portfolioStore.positions.length === 0">
                <td colspan="9" class="text-center text-muted">暂无持仓</td>
              </tr>
              <tr v-for="position in portfolioStore.positions" :key="position.id">
                <td>{{ position.asset_type }}</td>
                <td>{{ position.asset_code }}</td>
                <td>{{ position.asset_name || '-' }}</td>
                <td>{{ formatNumber(position.total_shares) }}</td>
                <td>{{ formatMoney(position.average_cost) }}</td>
                <td>{{ formatMoney(position.current_price) }}</td>
                <td>{{ formatMoney(position.current_value) }}</td>
                <td :class="pnlClass(position.unrealized_pnl_pct)">
                  {{ formatPercent(position.unrealized_pnl_pct) }}
                </td>
                <td class="text-end">
                  <button class="btn btn-outline-danger btn-sm" @click="handleDeletePosition(position.id)">
                    删除
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div class="card mb-4">
      <div class="card-header bg-white">
        <h5 class="mb-0">
          <i class="bi bi-receipt me-2"></i>
          交易记录
        </h5>
      </div>
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-2">
            <label class="form-label">类型</label>
            <select v-model="transactionForm.assetType" class="form-select">
              <option value="fund">基金</option>
              <option value="stock">股票</option>
            </select>
          </div>
          <div class="col-md-2">
            <label class="form-label">代码</label>
            <input v-model="transactionForm.assetCode" type="text" class="form-control" />
          </div>
          <div class="col-md-2">
            <label class="form-label">名称</label>
            <input v-model="transactionForm.assetName" type="text" class="form-control" />
          </div>
          <div class="col-md-2">
            <label class="form-label">类型</label>
            <select v-model="transactionForm.transactionType" class="form-select">
              <option value="buy">买入</option>
              <option value="sell">卖出</option>
              <option value="dividend">分红</option>
              <option value="split">拆分</option>
              <option value="transfer_in">转入</option>
              <option value="transfer_out">转出</option>
            </select>
          </div>
          <div class="col-md-2">
            <label class="form-label">份额</label>
            <input v-model.number="transactionForm.shares" type="number" min="0" class="form-control" />
          </div>
          <div class="col-md-2">
            <label class="form-label">价格</label>
            <input v-model.number="transactionForm.price" type="number" min="0" class="form-control" />
          </div>
        </div>
        <div class="row g-3 mt-2">
          <div class="col-md-2">
            <label class="form-label">总金额(可选)</label>
            <input v-model="transactionForm.totalAmount" type="number" min="0" class="form-control" />
          </div>
          <div class="col-md-2">
            <label class="form-label">手续费(可选)</label>
            <input v-model="transactionForm.fees" type="number" min="0" class="form-control" />
          </div>
          <div class="col-md-3">
            <label class="form-label">交易日期</label>
            <input v-model="transactionForm.transactionDate" type="date" class="form-control" />
          </div>
          <div class="col-md-4">
            <label class="form-label">备注</label>
            <input v-model="transactionForm.notes" type="text" class="form-control" />
          </div>
          <div class="col-md-1 d-flex align-items-end">
            <button class="btn btn-primary w-100" @click="handleCreateTransaction">新增</button>
          </div>
        </div>

        <div class="table-responsive mt-4">
          <table class="table align-middle">
            <thead>
              <tr>
                <th>日期</th>
                <th>资产</th>
                <th>类型</th>
                <th>份额</th>
                <th>价格</th>
                <th>金额</th>
                <th>手续费</th>
                <th class="text-end">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="portfolioStore.transactions.length === 0">
                <td colspan="8" class="text-center text-muted">暂无交易记录</td>
              </tr>
              <tr v-for="tx in portfolioStore.transactions" :key="tx.id">
                <td>{{ tx.transaction_date }}</td>
                <td>{{ tx.asset_name || tx.asset_code }}</td>
                <td>{{ transactionTypeLabel(tx.transaction_type) }}</td>
                <td>{{ formatNumber(tx.shares) }}</td>
                <td>{{ formatMoney(tx.price) }}</td>
                <td>{{ formatMoney(tx.total_amount) }}</td>
                <td>{{ formatMoney(tx.fees) }}</td>
                <td class="text-end">
                  <button class="btn btn-outline-danger btn-sm" @click="handleDeleteTransaction(tx.id)">
                    删除
                  </button>
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
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { usePortfolioStore } from '@/stores/portfolio'
import type { Portfolio } from '@/types'

const portfolioStore = usePortfolioStore()

type AssetType = 'fund' | 'stock'
type TransactionType = 'buy' | 'sell' | 'dividend' | 'split' | 'transfer_in' | 'transfer_out'

const message = reactive<{ text: string; type: string }>({
  text: '',
  type: 'alert-info'
})

const editingPortfolioId = ref<number | null>(null)

const portfolioForm = reactive({
  name: '',
  description: '',
  benchmarkCode: '000300.SH',
  isDefault: false
})

const positionForm = reactive({
  assetType: 'fund',
  assetCode: '',
  assetName: '',
  totalShares: 0,
  averageCost: 0,
  sector: '',
  notes: ''
})

const transactionForm = reactive({
  assetType: 'fund',
  assetCode: '',
  assetName: '',
  transactionType: 'buy',
  shares: 0,
  price: 0,
  totalAmount: '',
  fees: '',
  transactionDate: '',
  notes: ''
})

const activePortfolio = computed(() => {
  const fromList = portfolioStore.portfolios.find(p => p.id === portfolioStore.activePortfolioId)
  return fromList || portfolioStore.summary?.portfolio || null
})

const allocationByType = computed(() => {
  const allocation = portfolioStore.summary?.allocation?.by_type || {}
  return Object.entries(allocation).map(([key, value]) => ({
    key,
    value: Number(value)
  }))
})

const showMessage = (text: string, type = 'alert-info') => {
  message.text = text
  message.type = type
  setTimeout(() => {
    message.text = ''
  }, 3000)
}

const isDefaultPortfolio = (portfolio: Portfolio) => {
  return Boolean(portfolio.is_default)
}

const reloadPortfolios = async () => {
  const result = await portfolioStore.loadPortfolios()
  if (!result.success) {
    showMessage(result.message || '获取组合列表失败', 'alert-danger')
  }
}

const selectPortfolio = (portfolioId: number) => {
  portfolioStore.setActivePortfolio(portfolioId)
}

const startEditPortfolio = (portfolio: Portfolio) => {
  editingPortfolioId.value = portfolio.id
  portfolioForm.name = portfolio.name
  portfolioForm.description = portfolio.description || ''
  portfolioForm.benchmarkCode = portfolio.benchmark_code || '000300.SH'
  portfolioForm.isDefault = Boolean(portfolio.is_default)
}

const resetPortfolioForm = () => {
  editingPortfolioId.value = null
  portfolioForm.name = ''
  portfolioForm.description = ''
  portfolioForm.benchmarkCode = '000300.SH'
  portfolioForm.isDefault = false
}

const handleSubmitPortfolio = async () => {
  if (!portfolioForm.name.trim()) {
    showMessage('组合名称不能为空', 'alert-warning')
    return
  }

  const payload = {
    name: portfolioForm.name.trim(),
    description: portfolioForm.description?.trim() || undefined,
    benchmark_code: portfolioForm.benchmarkCode?.trim() || undefined,
    is_default: portfolioForm.isDefault
  }

  if (editingPortfolioId.value) {
    const result = await portfolioStore.updatePortfolio(editingPortfolioId.value, payload)
    if (result.success) {
      showMessage('组合更新成功', 'alert-success')
      await portfolioStore.loadPortfolios()
      await portfolioStore.refreshPortfolioData(editingPortfolioId.value)
      resetPortfolioForm()
      return
    }
    showMessage(result.message || '组合更新失败', 'alert-danger')
    return
  }

  const result = await portfolioStore.createPortfolio(payload)
  if (result.success) {
    showMessage('组合创建成功', 'alert-success')
    await portfolioStore.loadPortfolios()
    const newId = (result.data as any)?.id
    if (newId) {
      portfolioStore.setActivePortfolio(Number(newId))
    }
    resetPortfolioForm()
    return
  }
  showMessage(result.message || '组合创建失败', 'alert-danger')
}

const handleSetDefault = async (portfolioId: number) => {
  const result = await portfolioStore.setDefaultPortfolio(portfolioId)
  if (result.success) {
    showMessage('默认组合已更新', 'alert-success')
    await portfolioStore.loadPortfolios()
    return
  }
  showMessage(result.message || '设置默认组合失败', 'alert-danger')
}

const handleDeletePortfolio = async (portfolioId: number) => {
  if (!window.confirm('确定要删除该组合吗？相关持仓和交易记录将一并删除。')) {
    return
  }
  const result = await portfolioStore.deletePortfolio(portfolioId)
  if (result.success) {
    showMessage('组合删除成功', 'alert-success')
    await portfolioStore.loadPortfolios()
    if (portfolioStore.activePortfolioId === portfolioId) {
      const next = portfolioStore.portfolios[0]
      portfolioStore.setActivePortfolio(next ? next.id : null)
    }
    return
  }
  showMessage(result.message || '组合删除失败', 'alert-danger')
}

const handleCreatePosition = async () => {
  if (!portfolioStore.activePortfolioId) {
    showMessage('请先选择组合', 'alert-warning')
    return
  }
  if (!positionForm.assetCode) {
    showMessage('资产代码不能为空', 'alert-warning')
    return
  }
  if (Number(positionForm.totalShares) <= 0 || Number(positionForm.averageCost) <= 0) {
    showMessage('份额和均价必须大于0', 'alert-warning')
    return
  }

  const payload = {
    asset_type: positionForm.assetType as AssetType,
    asset_code: positionForm.assetCode.trim(),
    asset_name: positionForm.assetName?.trim() || undefined,
    total_shares: Number(positionForm.totalShares),
    average_cost: Number(positionForm.averageCost),
    sector: positionForm.sector?.trim() || undefined,
    notes: positionForm.notes?.trim() || undefined
  }

  const result = await portfolioStore.createPosition(portfolioStore.activePortfolioId, payload)
  if (result.success) {
    showMessage('持仓新增成功', 'alert-success')
    await portfolioStore.refreshPortfolioData(portfolioStore.activePortfolioId)
    positionForm.assetCode = ''
    positionForm.assetName = ''
    positionForm.totalShares = 0
    positionForm.averageCost = 0
    positionForm.sector = ''
    positionForm.notes = ''
    return
  }
  showMessage(result.message || '持仓新增失败', 'alert-danger')
}

const handleDeletePosition = async (positionId: number) => {
  if (!portfolioStore.activePortfolioId) {
    return
  }
  if (!window.confirm('确定要删除该持仓吗？')) {
    return
  }
  const result = await portfolioStore.deletePosition(portfolioStore.activePortfolioId, positionId)
  if (result.success) {
    showMessage('持仓删除成功', 'alert-success')
    await portfolioStore.refreshPortfolioData(portfolioStore.activePortfolioId)
    return
  }
  showMessage(result.message || '持仓删除失败', 'alert-danger')
}

const handleCreateTransaction = async () => {
  if (!portfolioStore.activePortfolioId) {
    showMessage('请先选择组合', 'alert-warning')
    return
  }
  if (!transactionForm.assetCode) {
    showMessage('资产代码不能为空', 'alert-warning')
    return
  }
  if (!transactionForm.transactionDate) {
    showMessage('交易日期不能为空', 'alert-warning')
    return
  }
  if (Number(transactionForm.shares) <= 0 || Number(transactionForm.price) <= 0) {
    showMessage('交易份额和价格必须大于0', 'alert-warning')
    return
  }

  const totalAmountValue = transactionForm.totalAmount === '' ? undefined : Number(transactionForm.totalAmount)
  const feesValue = transactionForm.fees === '' ? undefined : Number(transactionForm.fees)

  const payload = {
    asset_type: transactionForm.assetType as AssetType,
    asset_code: transactionForm.assetCode.trim(),
    asset_name: transactionForm.assetName?.trim() || undefined,
    transaction_type: transactionForm.transactionType as TransactionType,
    shares: Number(transactionForm.shares),
    price: Number(transactionForm.price),
    total_amount: Number.isFinite(totalAmountValue as number) ? totalAmountValue : undefined,
    fees: Number.isFinite(feesValue as number) ? feesValue : undefined,
    transaction_date: transactionForm.transactionDate,
    notes: transactionForm.notes?.trim() || undefined
  }

  const result = await portfolioStore.createTransaction(portfolioStore.activePortfolioId, payload)
  if (result.success) {
    showMessage('交易新增成功', 'alert-success')
    await portfolioStore.refreshPortfolioData(portfolioStore.activePortfolioId)
    transactionForm.assetCode = ''
    transactionForm.assetName = ''
    transactionForm.shares = 0
    transactionForm.price = 0
    transactionForm.totalAmount = ''
    transactionForm.fees = ''
    transactionForm.transactionDate = ''
    transactionForm.notes = ''
    return
  }
  showMessage(result.message || '交易新增失败', 'alert-danger')
}

const handleDeleteTransaction = async (transactionId: number) => {
  if (!portfolioStore.activePortfolioId) {
    return
  }
  if (!window.confirm('确定要删除该交易记录吗？')) {
    return
  }
  const result = await portfolioStore.deleteTransaction(portfolioStore.activePortfolioId, transactionId)
  if (result.success) {
    showMessage('交易删除成功', 'alert-success')
    await portfolioStore.refreshPortfolioData(portfolioStore.activePortfolioId)
    return
  }
  showMessage(result.message || '交易删除失败', 'alert-danger')
}

const formatMoney = (value?: number) => {
  if (value === null || value === undefined || Number.isNaN(value)) {
    return '-'
  }
  return Number(value).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const formatNumber = (value?: number) => {
  if (value === null || value === undefined || Number.isNaN(value)) {
    return '-'
  }
  return Number(value).toLocaleString(undefined, { maximumFractionDigits: 4 })
}

const formatPercent = (value?: number) => {
  if (value === null || value === undefined || Number.isNaN(value)) {
    return '-'
  }
  return `${Number(value).toFixed(2)}%`
}

const pnlClass = (value?: number) => {
  if (value === null || value === undefined || Number.isNaN(value)) {
    return 'text-muted'
  }
  return Number(value) >= 0 ? 'text-success fw-semibold' : 'text-danger fw-semibold'
}

const transactionTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    buy: '买入',
    sell: '卖出',
    dividend: '分红',
    split: '拆分',
    transfer_in: '转入',
    transfer_out: '转出'
  }
  return map[type] || type
}

watch(
  () => portfolioStore.activePortfolioId,
  async (portfolioId) => {
    if (portfolioId) {
      await portfolioStore.refreshPortfolioData(portfolioId)
    } else {
      portfolioStore.positions = []
      portfolioStore.transactions = []
      portfolioStore.summary = null
    }
  }
)

onMounted(async () => {
  const ensureResult = await portfolioStore.ensureDefaultPortfolio()
  if (!ensureResult.success) {
    showMessage(ensureResult.message || '初始化组合失败', 'alert-warning')
  }

  if (!portfolioStore.activePortfolioId && portfolioStore.portfolios.length > 0) {
    portfolioStore.setActivePortfolio(portfolioStore.portfolios[0].id)
  }
})
</script>

<template>
  <div class="container-fluid">
    <div class="row">
      <aside class="col-lg-2 col-md-3 bg-light border-end p-0 finance-sidebar">
        <div class="p-3 border-bottom">
          <h5 class="mb-0">
            <i class="bi bi-grid me-2"></i>
            金融功能
          </h5>
        </div>
        <div class="list-group list-group-flush">
          <router-link class="list-group-item list-group-item-action" to="/finance/portfolios">
            <i class="bi bi-pie-chart me-2"></i>
            基金组合
          </router-link>
          <router-link class="list-group-item list-group-item-action" to="/finance/fund-analysis">
            <i class="bi bi-bar-chart-line me-2"></i>
            基金分析
          </router-link>
          <router-link class="list-group-item list-group-item-action" to="/finance/stock-monitor">
            <i class="bi bi-activity me-2"></i>
            股票监控
          </router-link>
          <router-link class="list-group-item list-group-item-action" to="/finance/recommendations">
            <i class="bi bi-stars me-2"></i>
            AI推荐
          </router-link>
          <router-link class="list-group-item list-group-item-action" to="/finance/news">
            <i class="bi bi-newspaper me-2"></i>
            新闻资讯
          </router-link>
          <router-link class="list-group-item list-group-item-action" to="/finance/sentiment">
            <i class="bi bi-emoji-smile me-2"></i>
            情绪分析
          </router-link>
        </div>
      </aside>

      <main class="col-lg-10 col-md-9 p-4">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.finance-sidebar {
  min-height: calc(100vh - 64px);
}
</style>

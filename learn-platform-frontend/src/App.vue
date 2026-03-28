<template>
  <div id="app" :class="{ 'with-sidebar': showSidebar }">
    <Sidebar v-if="showSidebar" />
    
    <div class="main-layout flex-grow-1 d-flex flex-column min-vh-100">
      <NavBar v-if="showNavBar" />
      <main class="content-body flex-grow-1">
        <router-view />
      </main>
      <Footer v-if="showNavBar" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from './components/Sidebar.vue'
import NavBar from './components/NavBar.vue'
import Footer from './components/Footer.vue'

const route = useRoute()

const showSidebar = computed(() => {
  const agentRoutes = ['/explore', '/studio', '/chat']
  return agentRoutes.some(r => route.path.startsWith(r))
})

const showNavBar = computed(() => {
  const authRoutes = ['/login', '/register']
  return !authRoutes.includes(route.path)
})
</script>

<style>
#app {
  display: flex;
  min-height: 100vh;
  width: 100%;
}

.main-layout {
  flex: 1;
  min-width: 0;
  background-color: #fff;
  display: flex;
  flex-direction: column;
}

.content-body {
  position: relative;
}
</style>
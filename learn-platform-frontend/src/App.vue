<template>
  <div id="app">
    <NavBar v-if="showNavBar" />
    <div class="main-wrapper">
      <div class="content-wrapper">
        <router-view />
      </div>
    </div>
    <Footer v-if="showNavBar" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from './components/NavBar.vue'
import Footer from './components/Footer.vue'

const route = useRoute()

// Hide navigation on auth pages
const showNavBar = computed(() => {
  const authRoutes = ['/login', '/register']
  return !authRoutes.includes(route.path)
})
</script>
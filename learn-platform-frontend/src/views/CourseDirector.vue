<template>
  <div class="container py-5">
    <h1>Course Management Placeholder</h1>
    <p>This is a temporary placeholder to debug build issues.</p>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
const msg = ref('Hello')
onMounted(() => {
  console.log(msg.value)
})
</script>

<template>
  <div class="minimap-container" v-if="visible">
    <div class="minimap-card">
      <canvas ref="canvasRef" width="180" height="120"></canvas>
      <div class="minimap-info small text-muted text-center mt-1">
        {{ totalNodes }} 节点 / {{ totalLinks }} 边
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'

const props = defineProps<{
  visible: boolean
  totalNodes: number
  totalLinks: number
}>()

const canvasRef = ref<HTMLCanvasElement | null>(null)

onMounted(() => {
  drawMinimap()
})

watch(() => [props.totalNodes, props.totalLinks], () => {
  drawMinimap()
})

function drawMinimap() {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const w = canvas.width
  const h = canvas.height

  ctx.clearRect(0, 0, w, h)

  // Draw border
  ctx.strokeStyle = '#dee2e6'
  ctx.lineWidth = 1
  ctx.strokeRect(1, 1, w - 2, h - 2)

  // Draw grid dots pattern
  ctx.fillStyle = '#e9ecef'
  for (let x = 15; x < w; x += 20) {
    for (let y = 15; y < h; y += 20) {
      ctx.beginPath()
      ctx.arc(x, y, 1, 0, Math.PI * 2)
      ctx.fill()
    }
  }

  // Title
  ctx.fillStyle = '#6c757d'
  ctx.font = '10px sans-serif'
  ctx.textAlign = 'center'
  ctx.fillText('图谱导航', w / 2, h / 2 - 5)
  ctx.fillText('缩放/拖拽主图浏览', w / 2, h / 2 + 12)
}
</script>

<style scoped>
.minimap-container {
  position: absolute;
  bottom: 16px;
  left: 16px;
  z-index: 10;
}
.minimap-card {
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid #dee2e6;
  border-radius: 8px;
  padding: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
canvas {
  display: block;
  border-radius: 4px;
}
</style>

<template>
  <div class="ai-sidekick" :class="{ expanded }">
    <!-- Floating Button -->
    <button v-if="!expanded" class="btn btn-primary rounded-circle shadow ai-btn" @click="expanded = true">
      <i class="bi bi-robot fs-4"></i>
    </button>

    <!-- Chat Window -->
    <div v-if="expanded" class="card shadow ai-window">
      <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
        <h6 class="mb-0">
          <i class="bi bi-robot me-2"></i>
          学习助手
        </h6>
        <button class="btn btn-close btn-close-white" @click="expanded = false"></button>
      </div>

      <div class="card-body chat-body" ref="chatBody">
        <div v-if="messages.length === 0" class="text-center text-muted mt-5">
          <p>我是您的 AI 学习助手。针对本章内容有任何疑问，尽管问我吧！</p>
        </div>
        
        <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
          <div class="message-content">
            <div v-if="msg.role === 'ai'" class="text-pre-wrap">{{ msg.content }}</div>
            <div v-else>{{ msg.content }}</div>
          </div>
        </div>

        <div v-if="streaming" class="message ai">
          <div class="message-content">
            <div class="text-pre-wrap">{{ currentStream }}<span class="cursor">|</span></div>
          </div>
        </div>
      </div>

      <div class="card-footer bg-white border-top-0">
        <div class="input-group">
          <input
            v-model="userInput"
            type="text"
            class="form-control"
            placeholder="问问这个知识点..."
            @keyup.enter="sendMessage"
            :disabled="streaming"
          />
          <button class="btn btn-primary" @click="sendMessage" :disabled="streaming || !userInput.trim()">
            <i class="bi bi-send"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import { aiApi } from '@/services/api/ai'

const props = defineProps<{
  chapterId: string
}>()

const expanded = ref(false)
const streaming = ref(false)
const userInput = ref('')
const currentStream = ref('')
const chatBody = ref<HTMLElement | null>(null)

interface Message {
  role: 'user' | 'ai'
  content: string
}

const messages = ref<Message[]>([])

const scrollToBottom = async () => {
  await nextTick()
  if (chatBody.value) {
    chatBody.value.scrollTop = chatBody.value.scrollHeight
  }
}

watch(messages, scrollToBottom, { deep: true })
watch(currentStream, scrollToBottom)

const sendMessage = async () => {
  if (!userInput.value.trim() || streaming.value) return

  const question = userInput.value
  messages.value.push({ role: 'user', content: question })
  userInput.value = ''
  
  streaming.value = true
  currentStream.value = ''

  try {
    await aiApi.fetchAiChatStream(
      { chapterId: props.chapterId, message: question },
      (token) => {
        currentStream.value += token
      },
      () => {
        messages.value.push({ role: 'ai', content: currentStream.value })
        currentStream.value = ''
        streaming.value = false
      }
    )
  } catch (error) {
    messages.value.push({ role: 'ai', content: '抱歉，我现在遇到了一点问题，请稍后再试。' })
    streaming.value = false
  }
}
</script>

<style scoped>
.ai-sidekick {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 1050;
}

.ai-btn {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s;
}

.ai-btn:hover {
  transform: scale(1.1);
}

.ai-window {
  width: 350px;
  height: 500px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 12px;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  background-color: #f8f9fa;
}

.message {
  margin-bottom: 1rem;
  display: flex;
}

.message.user {
  justify-content: flex-end;
}

.message-content {
  max-width: 85%;
  padding: 0.6rem 1rem;
  border-radius: 12px;
  font-size: 0.9rem;
  line-height: 1.5;
}

.user .message-content {
  background-color: var(--primary-color, #0d6efd);
  color: white;
  border-bottom-right-radius: 2px;
}

.ai .message-content {
  background-color: white;
  color: #333;
  border-bottom-left-radius: 2px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.text-pre-wrap {
  white-space: pre-wrap;
  word-break: break-all;
}

.cursor {
  animation: blink 1s infinite;
  font-weight: bold;
  color: var(--primary-color, #0d6efd);
}

@keyframes blink {
  0% { opacity: 1; }
  50% { opacity: 0; }
  100% { opacity: 1; }
}

.card-footer {
  padding: 0.75rem;
}
</style>

<template>
  <div class="chat-container">
    <!-- Sidebar -->
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <button class="btn btn-new-chat w-100 shadow-sm" @click="startNewChat">
          <i class="bi bi-plus-lg me-2"></i>新对话
        </button>
      </div>
      <div class="conversation-list">
        <div 
          v-for="conv in conversations" 
          :key="conv.id"
          :class="['conv-item', { active: currentConvId === conv.id }]"
          @click="selectConversation(conv.id)"
        >
          <div class="conv-icon">
            <i class="bi bi-chat-left-text"></i>
          </div>
          <div class="conv-title">{{ conv.title }}</div>
          <div class="conv-date">{{ formatDate(conv.lastMessageAt) }}</div>
        </div>
      </div>
    </div>

    <!-- Main Chat Area -->
    <div class="chat-main" v-if="currentAgent">
      <div class="chat-header">
        <div class="agent-info">
          <div class="agent-avatar-small">
            <i v-if="!currentAgent.avatar" class="bi bi-robot"></i>
            <img v-else :src="currentAgent.avatar" class="avatar-img-small" />
          </div>
          <div class="agent-meta">
            <h5>{{ currentAgent.name }}</h5>
            <span>{{ currentAgent.modelId }}</span>
          </div>
        </div>
      </div>

      <div class="messages-container" ref="messageBox">
        <div v-if="messages.length === 0" class="welcome-screen text-center">
          <div class="welcome-icon">
             <i v-if="!currentAgent.avatar" class="bi bi-robot"></i>
             <img v-else :src="currentAgent.avatar" class="avatar-img-large" />
          </div>
          <h2>{{ currentAgent.name }}</h2>
          <p>{{ currentAgent.welcomeMessage || currentAgent.description }}</p>
          <div class="suggested-prompts mt-4">
            <button v-for="p in ['你好', '你能帮我做什么？', '解释一下 Java 泛型']" 
              :key="p" class="btn btn-outline-secondary btn-sm m-1" @click="userInput = p; sendMessage()">
              {{ p }}
            </button>
          </div>
        </div>

        <div v-for="msg in messages" :key="msg.id" :class="['message-row', msg.role]">
          <div class="message-avatar">
            <i :class="msg.role === 'user' ? 'bi bi-person' : 'bi bi-robot'"></i>
          </div>
          <div class="message-bubble">
            <div class="message-content markdown-body" v-html="renderMarkdown(msg.content)"></div>
          </div>
        </div>
        
        <!-- Streaming Message Holder -->
        <div v-if="isStreaming" class="message-row assistant">
          <div class="message-avatar">
            <i v-if="!currentAgent.avatar" class="bi bi-robot"></i>
            <img v-else :src="currentAgent.avatar" class="avatar-img-xs" />
          </div>
          <div class="message-bubble">
            <div class="message-content markdown-body" v-html="renderMarkdown(streamingContent)"></div>
          </div>
        </div>
      </div>

      <div class="chat-input-area">
        <div class="input-container">
          <textarea 
            class="form-control" 
            rows="1" 
            placeholder="输入您的问题..." 
            v-model="userInput"
            @keydown.enter.prevent="sendMessage"
          ></textarea>
          <button class="send-btn" :disabled="!userInput.trim() || isStreaming" @click="sendMessage">
            <i class="bi bi-send-fill"></i>
          </button>
        </div>
        <div class="input-footer">
          由 AI 生成的内容可能包含错误，请审慎核对。
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { agentApi, Agent, Conversation, Message } from '@/services/api/agent'
import { useAgentStore } from '@/stores/agent'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const route = useRoute()
const router = useRouter()
const agentStore = useAgentStore()
const agentId = computed(() => route.params.agentId as string)

const currentAgent = ref<Agent | null>(null)
const conversations = ref<Conversation[]>([])
const currentConvId = ref<string | null>(null)
const messages = ref<Message[]>([])
const userInput = ref('')
const isStreaming = ref(false)
const streamingContent = ref('')
const messageBox = ref<HTMLElement | null>(null)

const renderMarkdown = (content: string) => {
  return DOMPurify.sanitize(marked.parse(content || '') as string)
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString()
}

const scrollToBottom = async () => {
  await nextTick()
  if (messageBox.value) {
    messageBox.value.scrollTop = messageBox.value.scrollHeight
  }
}

const fetchAgent = async () => {
  // First try to get from store
  const agent = agentStore.getAgentById(agentId.value)
  if (agent) {
    currentAgent.value = agent
  } else {
    // If not in store, fetch list (e.g. on direct page access)
    await agentStore.fetchMyAgents()
    currentAgent.value = agentStore.getAgentById(agentId.value) || null
  }
}

const fetchConversations = async () => {
  try {
    const response = await agentApi.getConversations(agentId.value)
    conversations.value = response as any
    if (conversations.value.length > 0 && !currentConvId.value) {
      selectConversation(conversations.value[0].id)
    }
  } catch (err) {
    console.error('Failed to fetch conversations:', err)
  }
}

const selectConversation = async (id: string) => {
  currentConvId.value = id
  try {
    const response = await agentApi.getMessages(id)
    messages.value = response
    scrollToBottom()
  } catch (err) {
    console.error('Failed to select conversation:', err)
  }
}

const startNewChat = async () => {
  try {
    const response = await agentApi.startConversation(agentId.value, '新对话')
    conversations.value.unshift(response as any)
    await selectConversation(response.id)
    return response.id
  } catch (err) {
    console.error('Failed to start new chat:', err)
    return null
  }
}

const sendMessage = async () => {
  if (!userInput.value.trim() || isStreaming.value) return
  
  const content = userInput.value
  userInput.value = ''
  
  let convId = currentConvId.value
  if (!convId) {
    convId = await startNewChat()
    if (!convId) return // Failed to create conversation
  }

  // Add user message locally
  const tempId = 'temp-' + Date.now()
  messages.value.push({
    id: tempId,
    conversationId: convId,
    role: 'user',
    content,
    createdAt: new Date().toISOString()
  } as Message)
  scrollToBottom()

  isStreaming.value = true
  streamingContent.value = ''

  const es = agentApi.chatStream(agentId.value, convId, content, (token) => {
    streamingContent.value += token
    scrollToBottom()
  })

  es.onerror = async () => {
    // When stream finishes (or errors/closes), refresh messages to get the persisted assistant message
    if (streamingContent.value) {
       await selectConversation(currentConvId.value!)
    }
    isStreaming.value = false
    streamingContent.value = ''
    es.close()
  }
}

onMounted(() => {
  fetchAgent()
  fetchConversations()
})

watch(() => route.params.agentId, (newId) => {
  if (newId) {
    // Reset state for new agent
    currentConvId.value = null
    messages.value = []
    streamingContent.value = ''
    
    // Re-fetch data
    fetchAgent()
    fetchConversations()
  }
})

watch(streamingContent, () => {
  scrollToBottom()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  height: calc(100vh - 60px);
  background: #f8f9fa;
}

.chat-sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 1rem;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
}

.conv-item {
  padding: 0.75rem 1rem;
  cursor: pointer;
  border-bottom: 1px solid #f1f1f1;
  transition: all 0.2s;
}

.conv-item:hover {
  background: #f8f9fa;
}

.conv-item.active {
  background: #f3f4f6;
  border-left: 4px solid #6366f1;
}

.conv-title {
  font-size: 0.9rem;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conv-date {
  font-size: 0.7rem;
  color: #999;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.chat-header {
  padding: 1rem 2rem;
  border-bottom: 1px solid #eee;
  background: #fff;
}

.agent-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.agent-avatar-small {
  width: 32px;
  height: 32px;
  background: #eef5ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0d6efd;
  overflow: hidden;
}

.btn-new-chat {
  background: linear-gradient(135deg, #6366f1 0%, #a855f7 100%);
  color: white;
  border: none;
  font-weight: 600;
  border-radius: 10px;
  padding: 0.75rem;
  transition: all 0.3s ease;
}

.btn-new-chat:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
  filter: brightness(1.1);
}

.avatar-img-small, .avatar-img-xs {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-img-large {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 20px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.agent-meta h5 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.agent-meta span {
  font-size: 0.75rem;
  color: #999;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.message-row {
  display: flex;
  gap: 1rem;
  max-width: 80%;
}

.message-row.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-row.assistant .message-avatar {
  background: #eef5ff;
  color: #0d6efd;
}

.message-bubble {
  padding: 1rem;
  border-radius: 12px;
  background: #f8f9fa;
}

.message-row.user .message-bubble {
  background: #0d6efd;
  color: #fff;
}

.message-content {
  font-size: 0.95rem;
  line-height: 1.6;
}

.welcome-screen {
  margin-top: 10vh;
}

.welcome-icon {
  font-size: 4rem;
  color: #0d6efd;
  margin-bottom: 1rem;
}

.chat-input-area {
  padding: 1.5rem 2rem;
  border-top: 1px solid #eee;
}

.input-container {
  position: relative;
  max-width: 800px;
  margin: 0 auto;
}

.input-container textarea {
  padding: 1rem 3rem 1rem 1rem;
  border-radius: 12px;
  border: 1px solid #ddd;
  resize: none;
  font-size: 1rem;
}

.send-btn {
  position: absolute;
  right: 0.5rem;
  bottom: 0.5rem;
  background: #0d6efd;
  color: #fff;
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.send-btn:disabled {
  background: #ccc;
}

.input-footer {
  text-align: center;
  font-size: 0.75rem;
  color: #999;
  margin-top: 0.5rem;
}
</style>

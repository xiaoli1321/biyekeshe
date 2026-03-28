<template>
  <div class="workbench-overlay" v-if="!loading">
    <div class="workbench-modal-card shadow-lg">
      <!-- Header Area -->
      <div class="modal-header-section p-4 d-flex justify-content-between align-items-start">
        <div>
          <h2 class="fw-bold mb-1">{{ isNew ? '创建新的助理' : '编辑助手' }}</h2>
          <p class="text-muted small mb-0">配置你的智能助理，支持工具调用和知识库集成</p>
        </div>
        <button class="btn btn-sm btn-outline-secondary border-0" @click="goBack">取消</button>
      </div>

      <div class="workbench-main">
        <!-- Left: Configuration (Approx 3/5) -->
        <div class="workbench-config border-end">
          <!-- Tab Navigation (Centered) -->
          <div class="tabs-container px-4 py-2 border-bottom bg-light">
            <div class="nav nav-pills custom-pills justify-content-center">
              <button class="nav-link" :class="{ active: activeTab === 'basic' }" @click="activeTab = 'basic'">基础信息</button>
              <button class="nav-link" :class="{ active: activeTab === 'prompt' }" @click="activeTab = 'prompt'">提示词</button>
              <button class="nav-link" :class="{ active: activeTab === 'tools' }" @click="activeTab = 'tools'">工具 & 知识库</button>
            </div>
          </div>

          <div class="tabs-content p-4 flex-grow-1 overflow-auto">
            <!-- Tab 1: Basic Info -->
            <div v-show="activeTab === 'basic'">
               <div class="mb-4">
                 <h6 class="fw-bold mb-3">名称 & 头像</h6>
                 <div class="d-flex align-items-center gap-4 mb-4">
                    <div class="flex-grow-1">
                      <label class="form-label x-small text-muted fw-bold">名称</label>
                      <input type="text" class="form-control" v-model="form.name" placeholder="给你的功能性助理起个名字">
                    </div>
                    <div class="text-center">
                       <label class="form-label d-block x-small text-muted fw-bold">头像</label>
                       <div class="d-flex align-items-center gap-2">
                          <div class="avatar-circle shadow-sm" @click="triggerAvatarUpload">
                            <img v-if="form.avatar" :src="form.avatar" />
                            <i v-else class="bi bi-robot text-primary"></i>
                          </div>
                          <button class="btn btn-sm btn-outline-primary border py-1" @click="triggerAvatarUpload">
                            <i class="bi bi-upload pe-1"></i>上传头像
                          </button>
                       </div>
                    </div>
                 </div>
               </div>

               <div class="mb-4">
                 <h6 class="fw-bold mb-2">描述</h6>
                 <textarea class="form-control" rows="4" v-model="form.description" placeholder="输入功能性助理的描述"></textarea>
               </div>

               <div class="mb-4 text-center">
                 <h6 class="fw-bold mb-3 text-start">功能配置</h6>
                 <div class="row g-3">
                   <div class="col-md-6">
                      <div class="card border bg-light-subtle h-100">
                        <div class="card-body p-3 d-flex justify-content-between align-items-center">
                          <div class="d-flex align-items-center">
                            <div class="icon-square bg-success-subtle me-3">
                              <i class="bi bi-chat-dots-fill text-success"></i>
                            </div>
                            <div class="text-start">
                              <div class="fw-bold small">启用状态</div>
                              <div class="x-small text-muted">控制助理是否可以被使用</div>
                            </div>
                          </div>
                          <div class="form-check form-switch pt-1">
                            <input class="form-check-input" type="checkbox" v-model="form.enabled">
                          </div>
                        </div>
                      </div>
                   </div>
                   <div class="col-md-6">
                      <div class="card border bg-primary-subtle border-primary-subtle h-100">
                        <div class="card-body p-3">
                          <div class="d-flex align-items-center gap-2 mb-2">
                             <div class="icon-square-sm bg-primary text-white">
                               <i class="bi bi-cpu"></i>
                             </div>
                             <div class="fw-bold small">推理模型</div>
                          </div>
                          <select class="form-select form-select-sm shadow-none border-primary-subtle" v-model="form.modelId">
                            <option v-for="m in models" :key="m.id" :value="m.id">
                              {{ m.name }}
                            </option>
                          </select>
                        </div>
                      </div>
                   </div>
                 </div>
               </div>
            </div>

            <!-- Tab 2: Prompt -->
            <div v-show="activeTab === 'prompt'">
               <div class="mb-4">
                  <h6 class="fw-bold mb-3 d-flex justify-content-between align-items-center">
                    功能定义与指令
                    <button class="btn btn-outline-primary btn-sm" @click="helpPrompt">自动补全</button>
                  </h6>
                  <textarea class="form-control font-monospace mb-4" rows="10" v-model="form.systemPrompt" 
                    placeholder="输入详细的指令和运行逻辑..."></textarea>
               </div>
               <div class="mb-4">
                  <h6 class="fw-bold mb-2">欢迎消息</h6>
                  <input type="text" class="form-control" v-model="form.welcomeMessage" placeholder="在此输入第一句话...">
               </div>
            </div>

            <!-- Tab 3: Tools & Knowledge Base -->
             <div v-show="activeTab === 'tools'">
               <div class="mb-4">
                 <h6 class="fw-bold mb-3">知识库挂载 (RAG)</h6>
                 <div class="card border bg-light-subtle">
                   <div class="card-body p-4">
                     <div class="d-flex align-items-center gap-3 mb-3">
                       <div class="icon-square bg-info-subtle">
                         <i class="bi bi-database-fill text-info"></i>
                       </div>
                       <div>
                         <div class="fw-bold small">关联知识库空间</div>
                         <div class="x-small text-muted">启用后，助理在回答前会先检索该空间内的文档知识</div>
                       </div>
                     </div>
                     
                     <select class="form-select form-select-sm" v-model="form.kbCollectionId">
                       <option value="">不使用知识库</option>
                       <option v-for="kb in kbCollections" :key="kb.id" :value="kb.id">
                         {{ kb.name }}
                       </option>
                     </select>
                     
                     <div v-if="form.kbCollectionId" class="mt-3 p-3 bg-white border rounded-2 x-small">
                        <i class="bi bi-info-circle me-1"></i> 
                        当前已关联 <b>{{ kbCollections.find(k => k.id === form.kbCollectionId)?.name }}</b>。
                        助理将具备该空间下的文档检索能力。
                     </div>
                   </div>
                 </div>
               </div>

               <div class="text-center py-4 text-muted opacity-50 border-top mt-4">
                 <i class="bi bi-tools h4 d-block mb-2"></i>
                 <p class="x-small">更多工具 (Plugins/Actions) 将在后续版本开放</p>
               </div>
             </div>
          </div>

          <!-- Footer of Left Column -->
          <div class="left-footer p-4 border-top d-flex justify-content-end gap-3">
            <button class="btn btn-outline-secondary" @click="goBack">取消</button>
            <button class="btn btn-primary px-4" @click="saveAgent" :disabled="saving">
              {{ isNew ? '确认创建' : '保存更新' }}
            </button>
          </div>
        </div>

        <!-- Right: Preview (Approx 2/5) -->
        <div class="workbench-preview p-4 d-flex flex-column overflow-auto">
          <div class="mb-5">
            <h5 class="fw-bold mb-1">预览</h5>
            <p class="text-muted small">与你的 Agent 进行实时对话，预览实际效果</p>
            
            <div class="preview-box border rounded-3 bg-white mt-3 p-3 flex-grow-1 shadow-sm d-flex flex-column" style="min-height: 400px;">
               <div class="alert alert-primary-subtle border-0 py-2 x-small d-flex justify-content-between align-items-center">
                  <span>当前使用模型</span>
                  <a href="#" class="text-decoration-none">切换模型并应用到预览对话</a>
               </div>
               <div class="d-flex justify-content-between align-items-center mb-4">
                  <div class="d-flex align-items-center gap-2">
                    <div class="avatar-circle-sm bg-primary-subtle overflow-hidden">
                       <img v-if="form.avatar" :src="form.avatar" class="w-100 h-100 object-fit-cover" />
                       <span v-else>{{ form.name ? form.name.charAt(0) : '新' }}</span>
                    </div>
                    <div class="fw-bold small">{{ form.name || '新建助理' }} <div class="text-muted x-small fw-normal">预览模式</div></div>
                  </div>
                  <button class="btn btn-sm btn-outline-secondary border-0 text-muted x-small">清空对话</button>
               </div>
               
               <div class="chat-bubble-preview mb-4">
                  <div class="d-flex align-items-center gap-2 mb-1">
                    <i v-if="!form.avatar" class="bi bi-robot text-primary x-small"></i>
                    <img v-else :src="form.avatar" class="avatar-xs-preview rounded-circle" />
                    <span class="x-small fw-bold">{{ form.name || '新建助理' }} · <span class="text-muted fw-normal">Now</span></span>
                  </div>
                  <div class="bubble p-2 bg-light rounded-3 small border">
                    {{ form.welcomeMessage || '你好！我是你的AI助手，有什么可以协助您的？' }}
                    <i class="bi bi-copy float-end ms-2 text-muted x-small mt-1"></i>
                  </div>
               </div>

               <div class="mt-auto pt-3">
                  <div class="input-group input-group-sm">
                    <input type="text" class="form-control bg-light border-light" placeholder="输入消息进行预览...">
                    <button class="btn btn-primary"><i class="bi bi-send-fill"></i></button>
                  </div>
               </div>
            </div>
          </div>

          <div class="summary-section">
            <h6 class="fw-bold mb-3">配置摘要</h6>
            <div class="card border rounded-3">
              <div class="card-body p-0">
                <table class="table table-borderless table-sm mb-0 small">
                  <tbody>
                    <tr class="border-bottom">
                      <td class="p-2 text-muted px-3">类型</td>
                      <td class="p-2 text-end px-3 fw-bold">智能助理</td>
                    </tr>
                    <tr class="border-bottom">
                      <td class="p-2 text-muted px-3">工具数量</td>
                      <td class="p-2 text-end px-3 fw-bold">0</td>
                    </tr>
                    <tr class="border-bottom">
                      <td class="p-2 text-muted px-3">知识库数量</td>
                      <td class="p-2 text-end px-3 fw-bold">{{ form.kbCollectionId ? 1 : 0 }}</td>
                    </tr>
                    <tr>
                      <td class="p-2 text-muted px-3">状态</td>
                      <td class="p-2 text-end px-3"><span class="badge bg-success-subtle text-success rounded-pill px-2">启用</span></td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <input type="file" ref="avatarInput" @change="onAvatarChange" accept="image/*" class="d-none" />
  </div>
  <div v-else class="text-center py-5 mt-5">
    <div class="spinner-border text-primary"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { agentApi, Agent } from '@/services/api/agent'
import { getLlmProviders, LlmProviderDto } from '@/services/api/llm'
import { kbApi, KbCollection } from '@/services/api/kb'
import { useAgentStore } from '@/stores/agent'

const route = useRoute()
const router = useRouter()
const agentStore = useAgentStore()
const agentId = route.params.agentId as string
const isNew = computed(() => agentId === 'new')

const loading = ref(true)
const saving = ref(false)
const activeTab = ref('basic')
const avatarInput = ref<HTMLInputElement | null>(null)
const models = ref<LlmProviderDto[]>([])
const kbCollections = ref<KbCollection[]>([])

const form = ref<Partial<Agent>>({
  name: '',
  description: '',
  systemPrompt: '',
  welcomeMessage: '',
  modelId: 'default-v3',
  avatar: '',
  enabled: true
})

const goBack = () => router.push('/studio')

const fetchAgent = async () => {
  // Fetch lists first
  try {
    const [providers, collections] = await Promise.all([
      getLlmProviders(),
      kbApi.fetchCollections()
    ])
    models.value = providers
    kbCollections.value = collections
  } catch (err) {
    console.error('Failed to fetch config lists', err)
  }

  if (isNew.value) {
    loading.value = false
    return
  }
  try {
    loading.value = true
    const agentsList = await agentApi.getMyAgents()
    const agent = agentsList.find(a => a.id === agentId)
    if (agent) {
      form.value = { ...agent }
    } else {
      console.warn('Agent not found in MyAgents list, searching all...')
      const allAgents = await agentApi.getAgents()
      const fallbackAgent = allAgents.find(a => a.id === agentId)
      if (fallbackAgent) form.value = { ...fallbackAgent }
    }
  } catch (error) {
    console.error('Failed to fetch agent', error)
    router.push('/studio')
  } finally {
    loading.value = false
  }
}

const saveAgent = async () => {
  if (!form.value.name) return alert('请先设置智能体名称')
  try {
    saving.value = true
    if (isNew.value) {
      await agentApi.createAgent(form.value)
    } else {
      await agentApi.updateAgent(agentId, form.value)
    }
    await agentStore.fetchMyAgents() // Refresh global state
    router.push('/studio')
  } catch (error) {
    console.error(error)
    alert('保存出错')
  } finally {
    saving.value = false
  }
}

const triggerAvatarUpload = () => avatarInput.value?.click()

const onAvatarChange = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (event) => {
      form.value.avatar = event.target?.result as string
    }
    reader.readAsDataURL(file)
  }
}

const helpPrompt = () => {
  form.value.systemPrompt = `您是一个专业的 ${form.value.name || 'AI 助理'}。您的目标是：${form.value.description || '协助用户解决问题'}。`
}

onMounted(fetchAgent)
</script>

<style scoped>
.workbench-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 2rem;
}

.workbench-modal-card {
  width: 100%;
  max-width: 1200px;
  height: 90vh;
  background: #fff;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.workbench-main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.workbench-config {
  flex: 3;
  display: flex;
  flex-direction: column;
}

.workbench-preview {
  flex: 2;
  background: #fdfdfd;
}

.custom-pills .nav-link {
  background: #f8f9fa;
  margin: 0 4px;
  border-radius: 8px;
  padding: 6px 20px;
  color: #666;
  font-size: 0.9rem;
  border: 1px solid transparent;
}

.custom-pills .nav-link.active {
  background: #fff;
  color: #0d6efd;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  border-color: #eee;
}

.avatar-circle {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 2px solid #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  overflow: hidden;
  cursor: pointer;
  background: #fcfcfc;
}

.avatar-circle img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-circle-sm {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.75rem;
}

.icon-square {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.x-small { font-size: 0.75rem; }
.font-monospace { font-family: SFMono-Regular, Consolas, monospace; }

.bg-light-subtle { background-color: #fbfbfc; }
.alert-primary-subtle { background-color: #eef5ff; color: #0056b3; }

.chat-bubble-preview .bubble {
  position: relative;
  max-width: 100%;
}

.avatar-xs-preview {
  width: 18px;
  height: 18px;
  object-fit: cover;
}
</style>

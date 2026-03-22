<template>
  <div class="kb-container p-4">
    <div class="row h-100 g-4">
      <!-- 左侧知识库空间列表 -->
      <div class="col-md-3">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body p-3">
            <div class="d-flex justify-content-between align-items-center mb-4 pb-2 border-bottom">
              <h5 class="mb-0 fw-bold"><i class="bi bi-layers-half me-2"></i>知识库空间</h5>
              <button class="btn btn-sm btn-primary rounded-circle" @click="showCreateModal = true" title="新建空间">
                <i class="bi bi-plus"></i>
              </button>
            </div>
            
            <div class="list-group list-group-flush overflow-auto collection-list">
              <button 
                v-for="col in collections" 
                :key="col.id"
                class="list-group-item list-group-item-action border-0 mb-2 rounded px-3 py-2"
                :class="{ 'active': activeCollectionId === col.id }"
                @click="selectCollection(col.id)"
              >
                <div class="d-flex align-items-center">
                  <i class="bi" :class="activeCollectionId === col.id ? 'bi-folder2-open' : 'bi-folder2'"></i>
                  <div class="ms-3 overflow-hidden">
                    <div class="text-truncate fw-medium">{{ col.name }}</div>
                    <small class="text-muted d-block text-truncate" v-if="col.description">{{ col.description }}</small>
                  </div>
                </div>
              </button>
              <div v-if="collections.length === 0" class="text-muted text-center p-5">
                <i class="bi bi-plus-circle display-4 mb-3 d-block opacity-25"></i>
                <p>暂无空间，点击上方按钮创建</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧内容区 -->
      <div class="col-md-9 d-flex flex-column h-100">
        <div v-if="!activeCollectionId" class="flex-grow-1 card border-0 shadow-sm d-flex flex-column justify-content-center align-items-center text-muted">
          <img src="https://img.icons8.com/clouds/200/database.png" alt="welcome" class="mb-4 opacity-50">
          <h4>欢迎来到智能知识库</h4>
          <p>请在左侧选择一个空间，或者创建一个新的知识库来开始您的 RAG 之旅</p>
        </div>

        <template v-else>
          <!-- 顶部区域：空间信息 + 标签页 -->
          <div class="card border-0 shadow-sm mb-4">
            <div class="card-body py-3 px-4 d-flex justify-content-between align-items-center">
              <div class="d-flex align-items-center">
                <div class="bg-primary-subtle p-2 rounded me-3">
                  <i class="bi bi-database-fill text-primary fs-4"></i>
                </div>
                <div>
                  <h4 class="mb-0 fw-bold">{{ activeCollectionName }}</h4>
                  <small class="text-muted">已入库 {{ documents.length }} 个文档</small>
                </div>
              </div>
              
              <ul class="nav nav-pills" id="kbTab" role="tablist">
                <li class="nav-item">
                  <button 
                    class="nav-link px-4" 
                    :class="{ 'active': activeTab === 'manage' }"
                    @click="activeTab = 'manage'"
                  >
                    <i class="bi bi-gear-wide-connected me-2"></i>知识管理
                  </button>
                </li>
                <li class="nav-item ms-2">
                  <button 
                    class="nav-link px-4" 
                    :class="{ 'active': activeTab === 'chat' }"
                    @click="activeTab = 'chat'"
                  >
                    <i class="bi bi-chat-quote-fill me-2"></i>AI 对话
                  </button>
                </li>
              </ul>
            </div>
          </div>

          <div class="tab-content flex-grow-1 overflow-hidden">
            <!-- 知识管理面板 -->
            <div v-show="activeTab === 'manage'" class="h-100">
              <div class="d-flex flex-column h-100 p-1">
                <!-- 操作栏 -->
                <div class="d-flex gap-3 mb-4">
                  <button class="btn btn-primary px-4 py-2 shadow-sm" @click="triggerFileUpload">
                    <i class="bi bi-cloud-arrow-up-fill me-2"></i>上传文档(Word/PDF)
                  </button>
                  <button class="btn btn-outline-primary px-4 py-2" @click="showTextModal = true">
                    <i class="bi bi-pencil-square me-2"></i>手动录入
                  </button>
                  <input type="file" ref="fileInput" class="d-none" @change="handleFileUpload" accept=".pdf,.docx,.txt" />
                </div>

                <!-- 文档列表 -->
                <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden">
                  <div class="card-body p-0 overflow-auto">
                    <table class="table table-hover align-middle mb-0 custom-table">
                      <thead class="bg-light sticky-top">
                        <tr>
                          <th class="ps-4">数据集名称</th>
                          <th>格式</th>
                          <th>状态</th>
                          <th>创建时间</th>
                          <th class="text-end pe-4">操作</th>
                        </tr>
                      </thead>
                      <tbody>
                        <template v-for="doc in documents" :key="doc.id">
                          <tr>
                            <td class="ps-4">
                              <div class="d-flex align-items-center">
                                <i class="bi fs-5" :class="getFileIcon(doc.fileType)"></i>
                                <span class="ms-3 fw-medium text-dark">{{ doc.filename }}</span>
                              </div>
                            </td>
                            <td><span class="badge bg-light text-dark border">{{ doc.fileType.toUpperCase() }}</span></td>
                            <td>
                              <span v-if="doc.status === 'READY'" class="badge bg-success-subtle text-success border border-success-subtle px-3 rounded-pill">就绪</span>
                              <span v-else-if="doc.status === 'PARSING'" class="badge bg-warning-subtle text-warning border border-warning-subtle px-3 rounded-pill">
                                <span class="spinner-border spinner-border-sm me-1" style="width: 12px; height: 12px;"></span>解析中
                              </span>
                              <span v-else class="badge bg-danger-subtle text-danger border border-danger-subtle px-3 rounded-pill">失败</span>
                            </td>
                            <td><small class="text-muted">{{ formatDate(doc.uploadDate) }}</small></td>
                            <td class="text-end pe-4">
                              <button class="btn btn-sm btn-link text-decoration-none me-2" @click="toggleChunks(doc.id)">
                                <i class="bi" :class="expandedDocId === doc.id ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
                                {{ expandedDocId === doc.id ? '收起' : '详情' }}
                              </button>
                              <button class="btn btn-sm btn-link text-danger text-decoration-none" @click="deleteDoc(doc.id)">
                                <i class="bi bi-trash"></i>
                              </button>
                            </td>
                          </tr>
                          <!-- 展开显示原始数据分块 -->
                          <tr v-if="expandedDocId === doc.id">
                            <td colspan="5" class="bg-light p-0">
                              <div class="chunk-container p-4">
                                <div v-if="loadingChunks" class="text-center py-4">
                                  <div class="spinner-border text-primary" role="status"></div>
                                  <p class="mt-2 text-muted">正在加载数据分块与向量状态...</p>
                                </div>
                                <div v-else class="row g-3">
                                  <div v-for="chunk in chunks" :key="chunk.id" class="col-12">
                                    <div class="card border-0 shadow-sm chunk-card">
                                      <div class="card-body p-3">
                                        <div class="d-flex justify-content-between align-items-center mb-2">
                                          <span class="badge bg-secondary-subtle text-dark">#{{ chunk.chunkIndex + 1 }}</span>
                                          <span class="small text-success"><i class="bi bi-check-circle-fill me-1"></i>已向量化存储</span>
                                        </div>
                                        <p class="chunk-text mb-0">{{ chunk.content }}</p>
                                      </div>
                                    </div>
                                  </div>
                                  <div v-if="chunks.length === 0" class="col-12 text-center py-3 text-muted">
                                    此文档暂无有效分块数据
                                  </div>
                                </div>
                              </div>
                            </td>
                          </tr>
                        </template>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>

            <!-- AI 对话面板 -->
            <div v-show="activeTab === 'chat'" class="h-100">
              <div class="chat-layout d-flex flex-column h-100 bg-white rounded shadow-sm border overflow-hidden">
                <!-- 消息展示区 -->
                <div class="chat-body flex-grow-1 p-4 overflow-auto" ref="chatBox">
                  <div class="text-center mb-4">
                    <span class="badge bg-light text-muted px-3 py-2 rounded-pill border">
                      <i class="bi bi-shield-check me-1"></i> 您正在基于文档库 [{{ activeCollectionName }}] 进行 RAG 对话
                    </span>
                  </div>

                  <div v-for="(msg, idx) in chatMessages" :key="idx" class="d-flex mb-4" :class="msg.role === 'user' ? 'justify-content-end' : 'justify-content-start'">
                    <div class="avatar me-3" v-if="msg.role === 'assistant'">
                      <div class="bg-primary rounded-circle d-flex align-items-center justify-content-center text-white shadow-sm" style="width: 40px; height: 40px;">
                        <i class="bi bi-robot fs-5"></i>
                      </div>
                    </div>
                    <div class="msg-content p-3 rounded-4 shadow-sm" 
                         :class="msg.role === 'user' ? 'bg-primary text-white rounded-te-0' : 'bg-light text-dark rounded-ts-0'"
                         style="max-width: 80%;">
                      <div class="markdown-body" v-html="renderMarkdown(msg.content)"></div>
                    </div>
                    <div class="avatar ms-3" v-if="msg.role === 'user'">
                       <div class="bg-secondary rounded-circle d-flex align-items-center justify-content-center text-white shadow-sm" style="width: 40px; height: 40px;">
                        <i class="bi bi-person-fill fs-5"></i>
                      </div>
                    </div>
                  </div>
                  
                  <div v-if="isTyping" class="d-flex mb-4 align-items-center text-muted">
                    <div class="spinner-grow spinner-grow-sm me-2 text-primary"></div>
                    <span>AI 正在分析知识库并组织语言...</span>
                  </div>
                </div>

                <!-- 输入区 -->
                <div class="chat-footer p-3 bg-white border-top">
                  <div class="input-container d-flex align-items-center gap-2 p-2 rounded-4 border bg-light focus-within-shadow">
                    <textarea 
                      class="form-control border-0 bg-transparent flex-grow-1" 
                      v-model="userQuery" 
                      placeholder="询问当前知识库的内容..."
                      @keydown.enter.exact.prevent="sendQuery"
                      rows="1"
                    ></textarea>
                    <button class="btn btn-primary rounded-pill px-4 shadow-sm" @click="sendQuery" :disabled="!userQuery.trim() || isTyping">
                      <i class="bi bi-send-fill"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- 创建空间 Modal -->
    <div v-if="showCreateModal" class="modal-backdrop-custom d-flex align-items-center justify-content-center">
      <div class="modal-dialog-custom bg-white rounded-4 shadow-lg p-4 animate-pop">
        <h5 class="fw-bold mb-4"><i class="bi bi-folder-plus text-primary me-2"></i>新建知识库空间</h5>
        <div class="mb-3">
          <label class="form-label text-muted">空间名称</label>
          <input type="text" class="form-control rounded-3" v-model="newCollection.name" placeholder="请输入名称，如：考研复习资料库">
        </div>
        <div class="mb-4">
          <label class="form-label text-muted">描述 (可选)</label>
          <textarea class="form-control rounded-3" v-model="newCollection.description" rows="3" placeholder="简单描述一下这个空间存储的内容"></textarea>
        </div>
        <div class="d-flex gap-2 justify-content-end">
          <button class="btn btn-light rounded-pill px-4" @click="showCreateModal = false">取消</button>
          <button class="btn btn-primary rounded-pill px-4" @click="createCollection" :disabled="!newCollection.name">立即创建</button>
        </div>
      </div>
    </div>

    <!-- 手动文本录入 Modal -->
    <div v-if="showTextModal" class="modal-backdrop-custom d-flex align-items-center justify-content-center">
      <div class="modal-dialog-custom bg-white rounded-4 shadow-lg p-4 animate-pop" style="width: 800px;">
        <h5 class="fw-bold mb-4"><i class="bi bi-pencil-square text-primary me-2"></i>手动录入知识条目</h5>
        <div class="mb-3">
          <label class="form-label text-muted">标题</label>
          <input type="text" class="form-control rounded-3" v-model="textEntry.title" placeholder="输入知识片段的标题">
        </div>
        <div class="mb-4">
          <label class="form-label text-muted">内容 (系统会自动进行智能分块)</label>
          <textarea class="form-control rounded-3" v-model="textEntry.content" rows="15" placeholder="在此粘贴或输入您的知识内容..."></textarea>
        </div>
        <div class="d-flex gap-2 justify-content-end">
          <button class="btn btn-light rounded-pill px-4" @click="showTextModal = false">取消</button>
          <button class="btn btn-primary rounded-pill px-4" @click="submitText" :disabled="!textEntry.content">确认入库</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue';
import { kbApi, type KbDocument, type KbCollection, type KbChunk } from '@/services/api/kb';
import { marked } from 'marked';
import DOMPurify from 'dompurify';

// 基础状态
const collections = ref<KbCollection[]>([]);
const activeCollectionId = ref('');
const activeTab = ref('manage'); // 'manage' or 'chat'
const documents = ref<KbDocument[]>([]);
const userQuery = ref('');
const isTyping = ref(false);
const chatMessages = ref<any[]>([]);
const chatBox = ref<HTMLElement | null>(null);

// Modal 状态
const showCreateModal = ref(false);
const newCollection = ref({ name: '', description: '' });
const showTextModal = ref(false);
const textEntry = ref({ title: '', content: '' });

// 详情展开状态
const expandedDocId = ref('');
const chunks = ref<KbChunk[]>([]);
const loadingChunks = ref(false);

const fileInput = ref<HTMLInputElement | null>(null);

const activeCollectionName = computed(() => {
  return collections.value.find(c => c.id === activeCollectionId.value)?.name || '';
});

onMounted(async () => {
  await loadCollections();
});

const loadCollections = async () => {
  try {
    const list = await kbApi.fetchCollections();
    collections.value = list;
    if (list.length > 0 && !activeCollectionId.value) {
      await selectCollection(list[0].id);
    }
  } catch (e) {
    console.error('Failed to load collections', e);
  }
};

const selectCollection = async (id: string) => {
  activeCollectionId.value = id;
  expandedDocId.value = '';
  chatMessages.value = []; // 切换空间清空对话 (可选)
  await loadDocuments();
};

const loadDocuments = async () => {
  if (!activeCollectionId.value) return;
  try {
    documents.value = await kbApi.fetchDocuments(activeCollectionId.value);
  } catch (e) {
    console.error('Failed to load documents', e);
  }
};

const createCollection = async () => {
  try {
    const res = await kbApi.createCollection(newCollection.value.name, newCollection.value.description);
    collections.value.push(res);
    showCreateModal.value = false;
    newCollection.value = { name: '', description: '' };
    await selectCollection(res.id);
  } catch (e) {
    alert('创建失败');
  }
};

const triggerFileUpload = () => fileInput.value?.click();

const handleFileUpload = async (event: any) => {
  const file = event.target.files[0];
  if (!file || !activeCollectionId.value) return;
  
  try {
    await kbApi.uploadFile(file, activeCollectionId.value);
    await loadDocuments();
    event.target.value = '';
  } catch (e) {
    alert('上传解析失败');
  }
};

const submitText = async () => {
  if (!activeCollectionId.value) return;
  try {
    await kbApi.submitText(textEntry.value.title, textEntry.value.content, activeCollectionId.value);
    showTextModal.value = false;
    textEntry.value = { title: '', content: '' };
    await loadDocuments();
  } catch (e) {
    alert('提交失败');
  }
};

const toggleChunks = async (docId: string) => {
  if (expandedDocId.value === docId) {
    expandedDocId.value = '';
    return;
  }
  expandedDocId.value = docId;
  loadingChunks.value = true;
  try {
    chunks.value = await kbApi.fetchChunks(docId);
  } catch (e) {
    chunks.value = [];
  } finally {
    loadingChunks.value = false;
  }
};

const deleteDoc = async (id: string) => {
  if (confirm('确定从知识库中移除该文档及其所有衍生的数据块吗？此操作不可撤销。')) {
    await kbApi.deleteDocument(id);
    await loadDocuments();
  }
};

const sendQuery = async () => {
  if (!userQuery.value.trim() || isTyping.value || !activeCollectionId.value) return;

  const msg = userQuery.value;
  chatMessages.value.push({ role: 'user', content: msg });
  userQuery.value = '';
  isTyping.value = true;

  const assistantMsg = { role: 'assistant', content: '' };
  chatMessages.value.push(assistantMsg);

  try {
    await kbApi.fetchKbChatStream(msg, activeCollectionId.value, (token) => {
      assistantMsg.content += token;
      scrollToBottom();
    });
  } catch (e) {
    assistantMsg.content = '抱歉，检索知识库或模型推理时发生故障，请检查连接或重试。';
  } finally {
    isTyping.value = false;
    scrollToBottom();
  }
};

const scrollToBottom = async () => {
  await nextTick();
  if (chatBox.value) {
    chatBox.value.scrollTop = chatBox.value.scrollHeight;
  }
};

const renderMarkdown = (text: string) => {
  return DOMPurify.sanitize(marked(text) as string);
};

const getFileIcon = (fileType: string) => {
  switch (fileType.toLowerCase()) {
    case 'pdf': return 'bi-file-earmark-pdf-fill text-danger';
    case 'docx': return 'bi-file-earmark-word-fill text-primary';
    case 'txt': return 'bi-file-earmark-text-fill text-secondary';
    default: return 'bi-file-earmark-fill';
  }
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString();
};
</script>

<style scoped>
.kb-container {
  height: calc(100vh - 100px);
  background-color: #f5f7f9;
}

.collection-list {
  scrollbar-width: thin;
}

.list-group-item {
  transition: all 0.2s;
  cursor: pointer;
}

.list-group-item:hover {
  background-color: #f0f4f8;
}

.list-group-item.active {
  background-color: #e7f1ff;
  color: #0d6efd;
  border-left: 4px solid #0d6efd !important;
}

.custom-table th {
  font-weight: 600;
  font-size: 0.85rem;
  color: #6c757d;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.chunk-card {
  border-left: 3px solid #0d6efd;
  transition: transform 0.2s;
}

.chunk-card:hover {
  transform: translateX(5px);
}

.chunk-text {
  font-size: 0.9rem;
  line-height: 1.6;
  color: #4a5568;
  white-space: pre-wrap;
}

/* Chat Layout */
.markdown-body {
  font-size: 0.95rem;
  line-height: 1.7;
}

.avatar i {
  font-size: 1.2rem;
}

.msg-content {
  line-height: 1.5;
}

.focus-within-shadow:focus-within {
  box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
  border-color: #0d6efd !important;
}

/* Modal */
.modal-backdrop-custom {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.4);
  backdrop-filter: blur(4px);
  z-index: 1050;
}

.modal-dialog-custom {
  width: 500px;
  max-width: 90vw;
}

.animate-pop {
  animation: pop 0.3s cubic-bezier(0.18, 0.89, 0.32, 1.28);
}

@keyframes pop {
  0% { transform: scale(0.9); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}

textarea {
  resize: none;
}
</style>

<template>
  <div class="workflow-edit-page d-flex flex-column h-100">
    <!-- 1. 顶部面包屑导航 -->
    <div class="workflow-breadcrumb border-bottom px-4 py-2 bg-white flex-shrink-0">
      <nav class="d-flex align-items-center gap-2 small">
        <span class="text-muted hover-link cursor-pointer">AI 工作流</span>
        <i class="bi bi-chevron-right text-muted x-small"></i>
        <span class="text-dark fw-bold">创建 & 运行工作流</span>
      </nav>
    </div>

    <div class="workflow-page-container d-flex flex-row flex-grow-1 overflow-hidden" style="position: relative;">
      <!-- ================= Left Side: Canvas & Toolbar ================= -->
      <div class="workflow-main-area d-flex flex-column flex-grow-1 bg-light shadow-inner" style="width: 0; min-height: 0;">
        <!-- 工具栏 -->
        <div class="workflow-toolbar px-4 py-3 d-flex justify-content-between align-items-center bg-white border-bottom shadow-sm flex-shrink-0 position-relative z-index-10">
          <div class="d-flex align-items-center gap-3">
            <div class="input-group input-group-sm rounded-pill border overflow-hidden" style="width: 220px;">
              <span class="input-group-text bg-white border-0 text-muted"><i class="bi bi-layout-wtf"></i></span>
              <select v-model="selectedTemplateId" class="form-select border-0 bg-white fw-bold shadow-none" :disabled="isRunning">
                <option v-for="tpl in templates" :key="tpl.id" :value="tpl.id">{{ tpl.name }}</option>
              </select>
            </div>
            <button @click="addCustomStep" class="btn btn-sm btn-outline-primary rounded-pill px-3" :disabled="isRunning || selectedTemplateId !== 'custom'">
              <i class="bi bi-plus-lg me-1"></i> 添加节点
            </button>
          </div>

          <div class="d-flex align-items-center gap-3">
            <div v-if="isRunning" class="text-primary small fw-bold d-flex align-items-center gap-2 bg-primary bg-opacity-10 px-3 py-1 rounded-pill">
               <span class="spinner-grow spinner-grow-sm"></span> 执行节点 #{{ currentStepIndex + 1 }}
            </div>
            <button
              v-if="!isRunning"
              class="btn btn-primary rounded-pill px-4 shadow-sm hover-lift"
              :disabled="!canRun"
              @click="handleRunWrapper"
            >
              <i class="bi bi-play-fill me-1"></i> {{ isFinished && hasContent ? '重新执行' : '立即执行' }}
            </button>
            <button v-else class="btn btn-danger rounded-pill px-4 shadow-sm" @click="handleStop">
              <i class="bi bi-stop-fill me-1"></i> 停止执行
            </button>
          </div>
        </div>

        <!-- 编排画布 (横向滚动) -->
        <div class="workflow-canvas flex-grow-1 position-relative overflow-hidden" @click.self="activeStepIndex = null">
          <div class="canvas-scroller d-flex align-items-center gap-4 p-5 flex-nowrap overflow-auto h-100">
            
            <!-- 全局配置节点 -->
            <div class="builder-node-card config-node" :class="{ 'active': activeStepIndex === null }" @click="activeStepIndex = null">
               <div class="node-icon bg-warning text-white"><i class="bi bi-gear-fill"></i></div>
               <div class="node-content flex-grow-1 overflow-hidden ms-3">
                  <div class="node-title fw-bold text-dark">基础配置</div>
                  <div class="node-desc text-muted small text-truncate mt-1" :title="topic">{{ topic || '未设置主题' }}</div>
               </div>
               <div class="active-border"></div>
            </div>

            <div class="step-connector text-slate-300"><i class="bi bi-arrow-right"></i></div>

            <!-- 动态步骤节点卡片 -->
            <template v-for="(step, idx) in currentSteps" :key="step.id">
              <div class="builder-node-card step-node d-flex flex-column" 
                   :class="{ 
                      'active': activeStepIndex === idx, 
                      'running': idx === currentStepIndex && isRunning, 
                      'completed': idx < currentStepIndex || (isFinished && !isRunning) 
                   }"
                   :draggable="selectedTemplateId === 'custom' && !isRunning"
                   @click="activeStepIndex = idx"
                   @dragstart="onDragStart($event, idx)"
                   @dragenter="onDragEnter($event, idx)"
                   @dragover.prevent
                   @drop="onDrop($event, idx)"
                   @dragend="onDragEnd">
                   
                <!-- Header -->
                <div class="d-flex justify-content-between align-items-center w-100 mb-2">
                  <div class="d-flex align-items-center gap-2">
                    <span class="badge bg-slate-100 text-slate-700 rounded-pill px-2 py-1 shadow-sm">#{{ idx + 1 }}</span>
                    <span class="d-flex align-items-center gap-1 text-primary fw-bold small">
                      <i :class="getTypeIcon(step.type)"></i> {{ getTypeLabel(step.type) }}
                    </span>
                  </div>
                  <div class="d-flex gap-2 text-muted x-small hover-action-group" v-if="selectedTemplateId === 'custom' && !isRunning">
                    <i class="bi bi-trash cursor-pointer hover-danger fs-6" @click.stop="removeCustomStep(idx)" title="移除节点"></i>
                    <i class="bi bi-pencil-fill cursor-pointer hover-primary fs-6" @click.stop="activeStepIndex = idx" title="编辑节点"></i>
                    <i class="bi bi-files cursor-pointer hover-primary fs-6" @click.stop="copyCustomStep(idx)" title="复制节点"></i>
                    <i class="bi bi-box-arrow-up cursor-pointer hover-primary fs-6" title="导出节点"></i>
                  </div>
                </div>

                <!-- Body -->
                <div class="flex-grow-1 w-100 d-flex flex-column gap-2 mb-3">
                  <div class="bg-slate-50 rounded-3 px-3 py-2 border border-slate-100">
                    <div class="x-small text-slate-700 fw-bold mb-1">标题(2号)</div>
                    <div class="small text-dark text-truncate">{{ step.title || '标题(2号)' }}</div>
                  </div>
                  <div class="bg-slate-50 rounded-3 px-3 py-2 border border-slate-100 flex-grow-1 d-flex flex-column">
                    <div class="x-small text-slate-700 fw-bold mb-1">提示词</div>
                    <div class="x-small text-muted text-truncate-2" style="white-space: pre-wrap; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">{{ step.content || '请输入指引...' }}</div>
                  </div>
                </div>

                <!-- Footer -->
                <div class="d-flex justify-content-between align-items-center w-100 mt-auto">
                  <div class="d-flex gap-2">
                    <span class="badge rounded-pill border border-info text-info bg-white px-2 py-1 fw-bold" style="font-size: 0.65rem;">Deepseek_V3</span>
                    <span class="badge rounded-pill border border-success text-success bg-white px-2 py-1 fw-bold" style="font-size: 0.65rem;">全</span>
                  </div>
                  <!-- Check Circle -->
                  <div class="cursor-pointer d-flex align-items-center justify-content-center" @click.stop="toggleStepCheck(step)">
                    <i class="bi fs-5" :class="step.checked ? 'bi-check-circle-fill text-purple' : 'bi-circle text-slate-300 hover-purple'"></i>
                  </div>
                </div>

                <div class="active-border"></div>
              </div>
              <div v-if="idx < currentSteps.length - 1" class="step-connector text-slate-300"><i class="bi bi-arrow-right"></i></div>
            </template>

            <!-- 尾部占位 -->
            <div class="pe-5"></div>
          </div>
        </div>
      </div>

      <!-- ================= Right Side: Config & Result Panel ================= -->
      <div class="workflow-side-panel bg-white d-flex flex-column border-start flex-shrink-0" style="width: 440px; box-shadow: -4px 0 15px rgba(0,0,0,0.03); z-index: 20; min-height: 0;">
        <!-- Tabs -->
        <div class="panel-tabs d-flex border-bottom bg-slate-50 flex-shrink-0">
          <button class="btn flex-fill rounded-0 py-3 border-0 active-tab-transition" :class="activePanelTab === 'config' ? 'bg-white fw-bold text-primary border-bottom border-primary border-2 shadow-sm' : 'text-muted'" @click="activePanelTab = 'config'">
             <i class="bi bi-sliders me-1"></i> 节点配置
          </button>
          <button class="btn flex-fill rounded-0 py-3 border-0 active-tab-transition" :class="activePanelTab === 'result' ? 'bg-white fw-bold text-primary border-bottom border-primary border-2 shadow-sm' : 'text-muted'" @click="activePanelTab = 'result'">
             <i class="bi bi-lightbulb me-1"></i> 结果预览
          </button>
        </div>

        <!-- Panel Content -->
        <div class="panel-content flex-grow-1 overflow-auto p-4 custom-scrollbar" style="background-color: #fafbfc; min-height: 0;" ref="panelContentRef" @scroll="handlePanelScroll">
          
          <!-- CONFIG MODE -->
          <div v-show="activePanelTab === 'config'" class="config-mode-container fade-in h-100">
            <!-- Global Config -->
            <div v-if="activeStepIndex === null" class="config-section h-100">
              <h6 class="mb-4 fw-bold text-dark d-flex align-items-center gap-2"><div class="mini-icon-box bg-warning text-white"><i class="bi bi-gear-fill"></i></div> 全局基础配置</h6>
              
              <div class="card border-0 shadow-sm rounded-4 p-4 mt-3 bg-white">
                <div class="mb-4">
                  <label class="form-label small fw-bold text-slate-700">主题关键词 <span class="text-danger">*</span></label>
                  <input v-model="topic" type="text" class="form-control bg-slate-50 border-slate-200 focus-ring-primary py-2" placeholder="例如：人工智能在教育的应用" />
                </div>
                <div class="mb-0">
                  <label class="form-label small fw-bold text-slate-700">工作流报告名称</label>
                  <input v-model="workflowName" type="text" class="form-control bg-slate-50 border-slate-200 focus-ring-primary py-2" placeholder="可选报告标题..." />
                </div>
              </div>
            </div>

            <!-- Node Config -->
            <div v-else-if="activeStep" class="config-section">
              <div class="d-flex align-items-center mb-4 gap-3 bg-white p-3 rounded-4 shadow-sm">
                <div class="mini-icon-box shadow-sm" :class="getTypeIconClass(activeStep.type)"><span class="fw-bold small">#{{ activeStepIndex + 1 }}</span></div>
                <div class="flex-grow-1">
                   <select v-model="activeStep.type" class="form-select form-select-sm border-0 bg-transparent fw-bold text-dark shadow-none cursor-pointer fs-6 p-0 ms-1" style="width: auto;">
                     <option value="text">文字生成</option>
                     <option value="fixed">固定内容</option>
                     <option value="table">表格数据</option>
                   </select>
                </div>
              </div>

              <div class="card border-0 shadow-sm rounded-4 p-4 bg-white mb-4">
                <div class="mb-4">
                  <label class="form-label small fw-bold text-slate-700">步骤标题</label>
                  <input v-model="activeStep.title" type="text" class="form-control bg-slate-50 border-slate-200 focus-ring-primary py-2" placeholder="输入步骤标题..." />
                </div>

                <div class="mb-0 position-relative">
                  <div class="d-flex justify-content-between align-items-center mb-2">
                    <label class="form-label small fw-bold text-slate-700 mb-0">提示词 (Prompt) <span class="text-danger">*</span></label>
                    <i class="bi bi-mic text-muted hover-primary cursor-pointer text-slate-400"></i>
                  </div>
                  <textarea v-model="activeStep.content" class="form-control bg-slate-50 border-slate-200 focus-ring-primary px-3 py-3 font-monospace small" style="min-height: 180px; resize: vertical;" placeholder="输入具体指令，使用 [TOPIC] 插入主题变量"></textarea>
                </div>
              </div>
              
              <div class="card border-0 shadow-sm rounded-4 p-4 bg-white">
                <div class="mb-4">
                  <div class="d-flex justify-content-between align-items-center mb-2">
                    <label class="form-label small fw-bold text-slate-700 mb-0">系统提示词 (System Prompt)</label>
                    <i class="bi bi-mic text-muted hover-primary cursor-pointer text-slate-400"></i>
                  </div>
                  <textarea v-model="activeStep.systemPrompt" class="form-control bg-slate-50 border-slate-200 focus-ring-primary px-3 py-2 small" rows="2" placeholder="默认系统提示词，输入可覆盖..."></textarea>
                </div>

                <div class="mb-4">
                  <label class="form-label small fw-bold text-slate-700 d-flex align-items-center gap-2">模型 <i class="bi bi-stars text-primary"></i></label>
                  <select :value="activeStep.model || 'default-v3'" @change="handleModelChange" class="form-select bg-slate-50 border-slate-200 focus-ring-primary py-2 shadow-none cursor-pointer">
                    <option v-for="p in llmProviders" :key="p.id" :value="p.id">
                      {{ p.name }} {{ p.isCustom ? '(个人)' : '' }}
                    </option>
                    <option disabled>──────────</option>
                    <option value="ADD_NEW" class="text-primary fw-bold">➕ 注册新大模型源...</option>
                  </select>
                </div>

                <div class="mb-4">
                  <label class="form-label small fw-bold text-slate-700">上下文模式</label>
                  <select class="form-select bg-slate-100 border-0 text-muted shadow-none py-2" disabled>
                    <option>全: 全历史模式</option>
                  </select>
                </div>

                <div class="mb-4 border-top border-slate-100 pt-4">
                  <label class="form-label small fw-bold text-slate-700">是否为思维过程 (Thinking Process)</label>
                  <div class="text-slate-500 mb-2 lh-sm x-small">思维过程不会出现在生成的文件中，但会对下面的步骤结果产生影响</div>
                  <select v-model="activeStep.isThinkingProcess" class="form-select bg-slate-50 border-slate-200 focus-ring-primary py-2 shadow-none cursor-pointer">
                    <option :value="true">是</option>
                    <option :value="false">否</option>
                  </select>
                </div>

                <div class="d-flex align-items-center justify-content-between pt-1">
                  <span class="small fw-bold text-slate-700">启动搜索引擎</span>
                  <div class="d-flex align-items-center gap-2">
                    <div class="form-check form-switch m-0 d-flex align-items-center">
                      <input class="form-check-input mt-0 cursor-pointer" type="checkbox" role="switch" checked disabled>
                    </div>
                    <span class="small text-muted fw-bold">关</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- RESULT MODE -->
          <div v-show="activePanelTab === 'result'" class="result-mode-container h-100 d-flex flex-column fade-in bg-white p-3 rounded-4 shadow-sm border">
            <div v-if="!hasContent && !isRunning" class="empty-result text-center text-muted m-auto opacity-75">
              <div class="mb-4">
                 <img src="https://api.dicebear.com/7.x/bottts/svg?seed=result&backgroundColor=f8fafc" width="80" class="rounded-circle shadow-sm" alt="Waiting">
              </div>
              <h6 class="fw-bold mb-2">等待执行</h6>
              <p class="small px-4 text-slate-500">点击左上方「立即执行」后，生成的专业报告将在此处实时流式呈现。</p>
            </div>
            
            <div v-else class="results-scroller flex-grow-1" ref="stepsResultRef">
               <!-- Header inside scroller for context -->
               <div class="d-flex justify-content-between align-items-center mb-3 pb-2 border-bottom sticky-top bg-white pt-1" style="z-index: 5;">
                   <h6 class="mb-0 fw-bold text-dark"><i class="bi bi-file-earmark-richtext text-primary me-2"></i>生成结果</h6>
                   <div class="d-flex gap-2" v-if="isFinished && hasContent">
                      <button class="btn btn-sm btn-light rounded-circle text-muted" title="导出" @click="exportMarkdown"><i class="bi bi-download"></i></button>
                      <button class="btn btn-sm btn-outline-primary rounded-pill px-3 shadow-sm hover-lift" title="复制全部" @click="exportMarkdown">
                        <i class="bi bi-copy me-1"></i> 复制
                      </button>
                   </div>
               </div>

               <div class="px-1 pb-4">
                  <StepCard
                    v-for="(msg, idx) in stepMessages"
                    :key="msg.id"
                    :message="msg"
                    :is-current-step="idx === currentStepIndex && isRunning"
                    :is-running="isRunning"
                  />
                  <div v-if="isRunning" class="py-3 px-4 bg-primary bg-opacity-10 rounded-3 text-primary small d-flex align-items-center gap-3 mt-3 shadow-sm">
                    <span class="spinner-grow spinner-grow-sm"></span> 
                    <span class="fw-bold">大模型正在思考并生成回复...</span>
                  </div>
               </div>
            </div>
          </div>

        </div>
      </div>
    </div>
    <!-- 大模型注册弹窗 -->
    <div class="modal fade" ref="registerModalRef" tabindex="-1" aria-hidden="true" style="z-index: 1055;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg rounded-4">
          <div class="modal-header border-0 pb-0">
            <h5 class="modal-title fw-bold text-dark"><i class="bi bi-robot text-primary me-2"></i>注册私有模型源</h5>
            <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4">
            <div class="mb-3">
              <label class="form-label small fw-bold text-slate-700">显示名称 <span class="text-danger">*</span></label>
              <input v-model="registerForm.name" type="text" class="form-control bg-slate-50 focus-ring-primary" placeholder="例如：我的代理 Deepseek">
            </div>
            <div class="mb-3">
              <label class="form-label small fw-bold text-slate-700">模型代号 (Alias) <span class="text-danger">*</span></label>
              <input v-model="registerForm.modelAlias" type="text" class="form-control bg-slate-50 focus-ring-primary" placeholder="例如：deepseek-chat / glm-4">
            </div>
            <div class="mb-3">
              <label class="form-label small fw-bold text-slate-700">专属 API Key <span class="text-danger">*</span></label>
              <input v-model="registerForm.apiKey" type="password" class="form-control bg-slate-50 focus-ring-primary" placeholder="sk-...">
            </div>
            <div class="mb-3">
              <label class="form-label small fw-bold text-slate-700">调用基带网关 (Base URL)</label>
              <input v-model="registerForm.baseUrl" type="text" class="form-control bg-slate-50 focus-ring-primary" placeholder="https://api.deepseek.com/v1">
            </div>
          </div>
          <div class="modal-footer border-0 pt-0">
            <button type="button" class="btn btn-light rounded-pill px-4" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary rounded-pill px-4 shadow-sm" @click="submitProvider" :disabled="submittingProvider">
              <span v-if="submittingProvider" class="spinner-border spinner-border-sm me-1"></span> 保存并自动选定
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* 全局页面样式 */
.workflow-edit-page {
  height: calc(100vh - 56px); /* 假设 Navbar 高度为 56px */
  background-color: #f8fafc;
}
.hover-link:hover { color: var(--primary-color) !important; transition: color 0.2s; }
.x-small { font-size: 0.75rem; }
.hover-lift { transition: transform 0.2s ease, box-shadow 0.2s ease; }
.hover-lift:hover { transform: translateY(-1px); box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -1px rgba(0,0,0,0.06) !important; }
.cursor-pointer { cursor: pointer; }

/* Slate Color Palette Extension */
.bg-slate-50 { background-color: #f8fafc; }
.bg-slate-100 { background-color: #f1f5f9; }
.border-slate-100 { border-color: #f1f5f9 !important; }
.border-slate-200 { border-color: #e2e8f0 !important; }
.text-slate-300 { color: #cbd5e1 !important; }
.text-slate-400 { color: #94a3b8 !important; }
.text-slate-500 { color: #64748b !important; }
.text-slate-700 { color: #334155 !important; }

/* 侧边栏及右侧面板 */
.workflow-side-panel {
  box-shadow: -4px 0 15px -3px rgba(0,0,0,0.05);
}
.active-tab-transition { transition: all 0.2s ease-in-out; }
.fade-in { animation: fadeIn 0.3s ease-in-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(5px); } to { opacity: 1; transform: translateY(0); } }

.focus-ring-primary:focus {
  outline: none;
  border-color: #818cf8 !important;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.15);
  background-color: #fff !important;
}

.mini-icon-box {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.text-purple { color: #8b5cf6 !important; }
.hover-purple:hover { color: #8b5cf6 !important; }
.hover-primary:hover { color: var(--primary-color) !important; }

/* 自定义滚动条 */
.custom-scrollbar {
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 transparent;
}
.custom-scrollbar::-webkit-scrollbar { width: 6px; height: 6px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }

/* 画布内样式 */
.workflow-canvas {
  background-image: radial-gradient(#cbd5e1 1px, transparent 1px);
  background-size: 24px 24px;
}
.canvas-scroller {
  -ms-overflow-style: none;  
  scrollbar-width: none;  
}
.canvas-scroller::-webkit-scrollbar { display: none; }

.builder-node-card {
  width: 320px;
  height: 240px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  position: relative;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
  display: flex;
}
.builder-node-card.step-node {
  cursor: grab;
  align-items: flex-start;
}
.builder-node-card.step-node:active { cursor: grabbing; }
.builder-node-card.config-node {
  width: 240px;
  height: 100px;
  cursor: pointer;
  align-items: center;
}
.builder-node-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  border-color: #cbd5e1;
}
.builder-node-card.active {
  border-color: var(--primary-color);
  background-color: #f5f3ff;
}
.builder-node-card.active .active-border {
  position: absolute;
  top: -1px; left: -1px; right: -1px; bottom: -1px;
  border: 2px solid var(--primary-color);
  border-radius: 12px;
  pointer-events: none;
}
.builder-node-card.running {
  border-color: var(--primary-color);
  animation: border-pulse 1.5s infinite alternate;
}
@keyframes border-pulse {
  from { box-shadow: 0 0 0 0 rgba(79,70,229,0.4); }
  to { box-shadow: 0 0 0 4px rgba(79,70,229,0); }
}
.builder-node-card.completed { border-color: #10b981; }

.node-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.35rem;
  flex-shrink: 0;
}

.delete-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  background: white;
  border-radius: 50%;
  padding: 0;
  line-height: 1;
  font-size: 1.25rem;
  opacity: 0;
  transition: opacity 0.2s, color 0.2s;
  z-index: 5;
}
.builder-node-card:hover .delete-btn { opacity: 1; }
.hover-danger:hover { color: #ef4444 !important; }

.step-connector {
  font-size: 1.5rem;
  flex-shrink: 0;
  opacity: 0.8;
}
</style>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { Modal } from 'bootstrap'

// --- UUID Utils ---
const uuidv4 = () => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

// --- Imports ---
import StepCard from '@/components/workflow/StepCard.vue'
import { useWorkflowStore } from '@/stores/workflow'
import { getBuiltinTemplates } from '@/services/api/workflow'
import type { WorkflowTemplate } from '@/services/api/workflow'
import { getLlmProviders, createLlmProvider, type LlmProviderDto, type LlmProviderRequest } from '@/services/api/llm'

// --- Store ---
const workflowStore = useWorkflowStore()
const {
  isRunning,
  isFinished,
  stepMessages,
  currentStepIndex,
  hasContent
} = storeToRefs(workflowStore)

// --- State Management for Layout ---
const activeStepIndex = ref<number | null>(null) // null means global config
const activePanelTab = ref<'config' | 'result'>('config')

// --- Template Selection ---
const templates = ref<WorkflowTemplate[]>(getBuiltinTemplates())
const selectedTemplateId = ref('report_basic')
const selectedTemplate = computed(() =>
  templates.value.find(t => t.id === selectedTemplateId.value) || templates.value[0]
)

// --- Custom Steps Storage ---
const customSteps = ref<any[]>([
  { id: uuidv4(), type: 'text', title: '自定义步骤 1', content: '请针对 [TOPIC] 进行分析', isThinkingProcess: false, model: 'default-v3', systemPrompt: '' }
])

// --- Dynamic Models Source & Modal ---
const llmProviders = ref<LlmProviderDto[]>([])
const registerModalRef = ref<HTMLElement | null>(null)
let registerModalInstance: Modal | null = null
const submittingProvider = ref(false)

const registerForm = ref<LlmProviderRequest>({
  name: '',
  modelAlias: 'deepseek-chat',
  apiKey: '',
  baseUrl: 'https://api.deepseek.com/v1'
})

const route = useRoute()

onMounted(async () => {
  if (registerModalRef.value) {
    registerModalInstance = new Modal(registerModalRef.value)
  }
  try {
    llmProviders.value = await getLlmProviders()
  } catch (e) {
    console.error('Failed to load llm providers', e)
  }

  // === AI Workflow Injection: Load steps from agent chat ===
  if (route.query.from === 'ai') {
    try {
      const stepsJson = sessionStorage.getItem('ai_workflow_steps')
      const wfName = sessionStorage.getItem('ai_workflow_name') || ''
      if (stepsJson) {
        const aiSteps = JSON.parse(stepsJson)
        if (Array.isArray(aiSteps) && aiSteps.length > 0) {
          // Switch to custom template and inject AI-generated steps
          selectedTemplateId.value = 'custom'
          customSteps.value = aiSteps.map((s: any) => ({
            id: s.id || uuidv4(),
            type: s.type || 'text',
            title: s.title || '',
            content: s.content || '',
            isThinkingProcess: s.isThinkingProcess || false,
            model: s.model || 'default-v3',
            systemPrompt: s.systemPrompt || '',
            historyMode: s.historyMode || 'all'
          }))
          workflowName.value = wfName
          console.log('[AI Workflow] Injected', aiSteps.length, 'steps from agent chat')
        }
        // Clean up sessionStorage after loading
        sessionStorage.removeItem('ai_workflow_steps')
        sessionStorage.removeItem('ai_workflow_name')
      }
    } catch (e) {
      console.error('Failed to load AI-generated workflow steps:', e)
    }
  }
})

const handleModelChange = (e: Event) => {
  const target = e.target as HTMLSelectElement
  const val = target.value
  if (val === 'ADD_NEW') {
    // 恢复视觉选项防止露馅
    target.value = activeStep.value.model || 'default-v3'
    // 唤起表单
    registerForm.value = { name: '', modelAlias: 'deepseek-chat', apiKey: '', baseUrl: 'https://api.deepseek.com/v1' }
    registerModalInstance?.show()
  } else {
    activeStep.value.model = val
  }
}

const submitProvider = async () => {
  if (!registerForm.value.name || !registerForm.value.apiKey) {
    alert('请填写名称与秘钥！')
    return
  }
  submittingProvider.value = true
  try {
    const newP = await createLlmProvider(registerForm.value)
    llmProviders.value.push(newP)
    if (activeStep.value) {
      activeStep.value.model = newP.id
    }
    registerModalInstance?.hide()
  } catch(e: any) {
    alert('模型保存异常: ' + e.message)
  } finally {
    submittingProvider.value = false
  }
}

const currentSteps = computed(() => {
  if (selectedTemplateId.value === 'custom') {
    return customSteps.value
  }
  return selectedTemplate.value?.steps || []
})

// --- Active Step Computed ---
const activeStep = computed(() => {
  if (activeStepIndex.value !== null && currentSteps.value[activeStepIndex.value]) {
    return currentSteps.value[activeStepIndex.value]
  }
  return null
})

// --- Actions ---
const addCustomStep = () => {
  customSteps.value.push({
    id: uuidv4(),
    type: 'text',
    title: `自定义步骤 ${customSteps.value.length + 1}`,
    content: '',
    isThinkingProcess: false,
    model: 'default-v3',
    systemPrompt: ''
  })
  // Select newly added step
  activeStepIndex.value = customSteps.value.length - 1
  activePanelTab.value = 'config'
}

const removeCustomStep = (idx: number) => {
  customSteps.value.splice(idx, 1)
  if (activeStepIndex.value === idx) {
    activeStepIndex.value = null
  } else if (activeStepIndex.value !== null && activeStepIndex.value > idx) {
    activeStepIndex.value = activeStepIndex.value - 1
  }
}

const copyCustomStep = (idx: number) => {
  if (selectedTemplateId.value !== 'custom') return
  const original = currentSteps.value[idx]
  const stepToCopy = JSON.parse(JSON.stringify(original))
  stepToCopy.id = uuidv4() // generate new element id
  stepToCopy.title = `${stepToCopy.title} (副本)`
  customSteps.value.splice(idx + 1, 0, stepToCopy)
  activeStepIndex.value = idx + 1
}

const toggleStepCheck = (step: any) => {
  step.checked = !step.checked
}

// --- Drag and Drop ---
const draggedIndex = ref<number | null>(null)

const onDragStart = (e: DragEvent, idx: number) => {
  if (isRunning.value || selectedTemplateId.value !== 'custom') return e.preventDefault()
  draggedIndex.value = idx
  if (e.dataTransfer) {
    e.dataTransfer.effectAllowed = 'move'
  }
}

const onDragEnter = (e: DragEvent, idx: number) => {
  if (draggedIndex.value === null || draggedIndex.value === idx) return
  if (selectedTemplateId.value !== 'custom') return

  // Swap Elements directly within customSteps
  const draggedItem = customSteps.value[draggedIndex.value]
  customSteps.value.splice(draggedIndex.value, 1)
  customSteps.value.splice(idx, 0, draggedItem)
  
  // Adjust select pointer to prevent UI selection flash
  if (activeStepIndex.value === draggedIndex.value) {
    activeStepIndex.value = idx
  } else if (activeStepIndex.value === idx) {
    activeStepIndex.value = draggedIndex.value
  }
  
  draggedIndex.value = idx
}

const onDrop = (e: DragEvent, idx: number) => {
  draggedIndex.value = null
}

const onDragEnd = () => {
  draggedIndex.value = null
}

// Watch template change to reset selection
watch(selectedTemplateId, () => {
  activeStepIndex.value = null
  activePanelTab.value = 'config'
})

// --- Form Variables (Global) ---
const topic = ref('')
const workflowName = ref('')

const canRun = computed(() => currentSteps.value.length > 0 && topic.value.trim().length > 0)

const stepsResultRef = ref<HTMLElement | null>(null)
const panelContentRef = ref<HTMLElement | null>(null)
const isUserScrolledUp = ref(false)

const handlePanelScroll = (e: Event) => {
  const el = e.target as HTMLElement
  // 判断用户是否正在阅览历史：当前视口距绝对底部的距离大于 50px
  const distanceToBottom = el.scrollHeight - el.scrollTop - el.clientHeight
  isUserScrolledUp.value = distanceToBottom > 50
}

// --- Run Logic ---
const handleRunWrapper = () => {
  activePanelTab.value = 'result'
  handleRun()
}

const handleRun = async () => {
  workflowStore.reset()
  
  const id = uuidv4()
  const topicVar = topic.value.trim()

  const processedSteps = currentSteps.value.map(s => ({
    ...s,
    content: s.content?.replace(/\[TOPIC\]/g, topicVar) || s.content,
    title: s.title?.replace(/\[TOPIC\]/g, topicVar) || s.title,
  }))

  await workflowStore.runWorkflow({
    rawMsgList: processedSteps,
    streamingId: id,
    generateFile: false,
    workflowName: workflowName.value || topicVar,
    returnMode: 'api'
  })
}

const handleStop = () => {
  workflowStore.stopWorkflow()
}

const exportMarkdown = () => {
  const md = workflowStore.fullMarkdown
  const blob = new Blob([md], { type: 'text/markdown;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `${workflowName.value || '工作流报告'}.md`
  a.click()
  URL.revokeObjectURL(a.href)
}

// Auto scroll in result tab
watch(
  () => workflowStore.stepMessages,
  async () => {
    // 若当前不在结果预览模式或节点未挂载则跳过
    if (activePanelTab.value !== 'result' || !panelContentRef.value) return

    // 只有在用户没有悬停阅读历史报告的前提下（或者已滑回底部），才开启重力吸附到最新行
    if (!isUserScrolledUp.value) {
      await nextTick()
      panelContentRef.value.scrollTop = panelContentRef.value.scrollHeight
    }
  },
  { deep: true }
)

// --- Visual Helpers ---
const getTypeIcon = (type: string) => {
  if (type === 'text') return 'bi-chat-left-text'
  if (type === 'fixed') return 'bi-pin-angle-fill'
  if (type === 'table') return 'bi-table'
  return 'bi-puzzle'
}

const getTypeIconClass = (type: string) => {
  if (type === 'text') return 'bg-primary text-white'
  if (type === 'fixed') return 'bg-success text-white'
  if (type === 'table') return 'bg-info text-white'
  return 'bg-secondary text-white'
}

const getTypeLabel = (type: string) => {
  if (type === 'text') return '语言生成'
  if (type === 'fixed') return '模板内容'
  if (type === 'table') return '表格解析'
  return '未知节点'
}
</script>

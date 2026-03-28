# 项目架构全景图 (Universal Agent Platform - Project Overview)

## 1. 核心技术栈 (Technology Stack)
- **后端 (Backend)**: Java 17 + Spring Boot 3.3.x (WebFlux 对齐流式响应)
- **数据库 (Database)**: MongoDB (存储 Agent 配置, 对话历史)
- **AI 编排 (AI Orchestration)**: LangChain4j (核心调用/提示词工程)
- **前端 (Frontend)**: Vue 3 + Vite + TypeScript (AgentX 仿生设计风格)
- **通信 (Communication)**: SSE (Server-Sent Events) 实现在线打字机对话效果

## 2. 核心模块与目录结构 (Core Modules & Files)
- **Backend /src/main/java/com/learnplatform/**:
  - `entity/`: 核心模型 (Agent, Conversation, Message)
  - `service/`: AI 核心逻辑, 包含 "The N+1 Strategy" 上下文管理
  - `controller/api/`: 提供 REST & SSE 接口
  - `config/`: Security (JWT), Data Initialization
- **Frontend /learn-platform-frontend/**:
  - `src/views/Studio.vue`: 智能体工作室 (管理大卡片列表)
  - `src/views/AgentEdit.vue`: 工作台弹窗 (大卡片设计风格)
  - `src/components/Sidebar.vue`: 沉浸式全局侧边栏导航

## 3. 关键业务流 (Critical Flows)
- **Agent CRUD**: 用户在 Studio 创建 Agent -> 存入 MongoDB -> Explore 同步显示。
- **Agent Dialogue Flow**:
  1. `Studio.vue` 点击卡片 -> 跳转 `/chat/:agentId`。
  2. `AgentChat.vue` 初始化会话列表 -> SSE 指令 `chatStream` -> 打字机渲染。
  3. **上下文管理 (The N+1 Strategy)**: 后端 `AgentService.buildContext` 自动提取最近 10 条历史 + 滑动窗口，确保 LLM 具备有效记忆的同时维持 Token 效率。
  4. **持久化策略**: 用户消息即时保存，助手消息在 SSE `doOnComplete` 时聚合持久化。

## 4. 开发红线与设计准则 (Design Principles)
- **视觉风格**: 必须匹配 AgentX 的“专业、深沉、大卡片”美学。
- **权限边界**: 严禁通过 API 修改非本人所属的 Agent，后端必须核验 `userId`。
- **代码复用**: 公共 UI 组件 (如 Avatar, Action Menu) 需在 `components/` 提取。
- **文档维护**: 每当新增 API 或 Entity 字段，必须同步更新本 `ARCH_OVERVIEW.md`。

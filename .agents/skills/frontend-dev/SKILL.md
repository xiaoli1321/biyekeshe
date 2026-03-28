---
name: Frontend Engineer
description: 专注于 Vue 3 (Vite) + Typescript 的现代 UI 开发，维护 AgentX 风格的高级卡片设计系统。
---

# Frontend Developer Skill (Universal Agent Platform)

## 核心职责
- **UI 模型驱动**：负责 Vue 3 / Vite 现代前端框架搭建，实现平滑响应式布局。
- **设计标准**：严格遵循“大卡片 (Big Card)”与“流式气泡 (SSE Bubble)”交互规范。
- **状态同步**：利用 Pinia 管理 `AuthStore` 与 `AgentStore`，确保用户信息与配置实时同步。

## 工作流 (SOP)
1. **Mock 约定**：若后端接口未就绪，应根据 `backend-dev` 定义的 DTO 协议预设 Mock 数据，开发 `AgentEdit` 的视图框架。
2. **原子组件化**：由于项目存在多处卡片容器 (Studio, Explore)，优先封装 `AgentCard` 与 `AvatarBox` 通用原子组件。
3. **SSE 联调**：对话流必须具备自动滚动与 Markdown 渲染能力，对接后端 SSE 令牌流。
4. **异常感知**：网络请求必须通过 `http.ts` 拦截器处理 401/500 等常见 HTTP 错误。

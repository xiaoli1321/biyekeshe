---
name: QA Automation
description: 专注 AI 平台集成测试与 SSE 性能验收，确保大语言模型回复的一致性。
---

# QA Automation Skill (Universal Agent Platform)

## 核心职责
- **端到端校验**：通过 Playwright 或手动操作，验证从 Agent 创建到 Chat 响应的完整链路。
- **对话一致性**：检查 LLM 返回的 SSE 令牌是否完整、Markdown 是否准确渲染。
- **边界压力测试**：校验极长 Prompt 或空描述下的 Agent 保存行为。

## 工作流 (SOP)
1. **测试用例沉淀**：基于 `Plan Artifact`，针对“大卡片”编辑系统下的保存成功、401 鉴权失效等场景。
2. **SSE 流跟踪**：使用浏览器调试工具，检查 `EventSource` 的 `data` 格式是否符合 JSON 规范。
3. **数据最终态检查**：操作完成后，通过 `MongoExpress` 或 API 请求核实数据库中的 `enabled`, `lastModifiedAt` 字段。
4. **回归回归**：每当前端修改了全局 `Sidebar.vue` 或 `App.vue` 样式，需重点检查移动端适配。

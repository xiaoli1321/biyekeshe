# 知识库功能设计与开发计划 (Knowledge Base Functional Design)

## 1. 后端功能设计 (Developer's Plan)
- **API 接口实现**：
    - `POST /api/kb/documents/upload`：文件上传接口，支持 `.pdf`, `.docx`, `.txt`。
    - `POST /api/kb/documents/text`：手动输入文本段落接口。
    - `GET /api/kb/documents`：获取已上传文档列表。
    - `DELETE /api/kb/documents/{id}`：删除文档及关联 Chunk。
    - `POST /api/kb/chat`：SSE 流式问答接口，接收 `query`，整合召回结果。
- **数据存储设计**：
    - `kb_documents` 集合：元数据（文件名、状态、时间）。
    - `kb_chunks` 集合：分块内容、所属文档 ID、索引权重。
- **混合检索逻辑**：
    - 关键词检索使用 MongoDB `$text` 查询。
    - 向量检索初步采用 Mock 向量（随机生成或简单的余弦相似度计算，待 API 秘钥齐备后接入正式 Embedding）。
    - 重排序初步采用基于关键词匹配分数的加权排布。

## 2. 前端功能设计 (Frontend Implementation)
- **知识库管理 (`/kb/manage`)**：
    - 采用 Bootstrap 5 构建清爽的卡片/列表布局。
    - **上传组件**：文件拖拽上传，显示实时解析状态（解析中/已就绪）。
    - **文本录入**：提供双语（中英）文本框，支持直接粘贴长文本。
- **AI 智能问答 (`/kb/chat`)**：
    - 模拟常见的大模型对话架构。
    - **气泡式对话展示**：区分用户与 AI。
    - **SSE 流式渲染**：前端使用 `EventSource` 或 `fetch` 的 `ReadableStream` 来处理服务器推送，实现逐字打印效果。
    - **参考溯源**：对话气泡下方展示“参考片段”，点击可弹窗查看原文。

## 3. 开发优先级
1. 后端解析服务与分块持久化。
2. 基础检索接口与问答骨架。
3. 前端上传与列表展示。
4. 前端问答 UI 与 SSE 对接。
5. 检索算法调优（向量化、Reranking）。
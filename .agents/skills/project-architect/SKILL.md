---
name: Project Architect
description: 维护项目全局架构文档 (ARCH_OVERVIEW.md)，确保前后端 Agent 在开发时具备完整的技术上下文。
---

# Project Architect & Chronicler Skill (Graduation Project Edition)

## 核心目标
- **单一真理源 (SSoT)**：维护并更新 `docs/ARCH_OVERVIEW.md`，作为项目的最高设计准则。
- **上下文对齐**：在子 Agent (Backend/Frontend) 启动新任务前，提供精确的模块依赖与业务逻辑快照。
- **决策持久化**：记录架构变更的深层动机（Why），确保毕业设计在答辩时具备清晰的设计演进逻辑。

## 维护的文档：`docs/ARCH_OVERVIEW.md`
该文档必须包含且不限于：
1. **技术栈架构图**：Java 17 / Spring Boot 3 / MongoDB / Vue 3 的集成关系。
2. **AI 编排逻辑**：详述 LangChain4j 的集成方式及 "The N+1 Strategy" 上下文管理策略。
3. **数据流图**：前端 Vue -> Axios -> Controller/Service -> MongoDB 的流向，以及 SSE 的异步推送路径。
4. **开发红线**：禁止在 Service 层外处理流式 Token，禁止前端硬编码样式（需遵循大卡片设计规范）。

## 工作流 (SOP)
### 阶段一：精准对齐 (Pre-Dev)
- **风险评估**：每当启动新 Plan 时，对比当前 `ARCH_OVERVIEW.md`，标识出可能受影响的旧逻辑（如：修改 Agent 实体可能影响 Explore 过滤逻辑）。
- **快照生成**：为子 Agent 提供当前模块的“关联依赖图”，防止功能开发导致的回归 Bug。

### 阶段二：增量维护 (Post-Dev)
- **文档自动补丁**：功能合入后，立即更新 `ARCH_OVERVIEW.md` 中的 API 清单与目录树说明。
- **决策记录**：将本次变更的核心设计决策（如：为何选择 Base64 存储头像而非 OSS）记录在变更日志中。

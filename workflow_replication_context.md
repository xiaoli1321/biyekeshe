# 上下文：LLM 驱动的多步骤 Word 工作流系统复刻指南

## 项目背景

这是一个基于 **Node.js + Express + Socket.IO + MongoDB** 的后端服务。
核心功能：用户提交一个「工作流步骤列表」，系统按顺序调用大模型（GLM/Deepseek/Qwen 等），
逐步生成包含「文字、表格、思维导图、ECharts 图表、图片」的富文本内容，
最终可汇总成一份 Word (.docx) 文件返回给用户。

所有内容通过 **HTTP Chunked 流式传输** 或 **WebSocket** 实时推送给前端，实现"打字机"效果。

---

## 技术栈

- **运行时**: Node.js
- **框架**: Express.js
- **实时通信**: Socket.IO ([io](file:///e:/conputer/wrokProject/midlleearth_tax_new/midlleearth_tax_new3/middleearth_tax_new/src/routes/llm.js#97-100), [socket](file:///e:/conputer/wrokProject/midlleearth_tax_new/midlleearth_tax_new3/middleearth_tax_new/src/controller/llm/apis/wf_apis.js#96-107))
- **数据库**: MongoDB (通过 Mongoose)
- **鉴权**: JWT 中间件
- **多模型支持**: GLM-4, Deepseek-V3, DeepSeek-R1, Qwen, Hunyuan, Kimi, Doubao, 自定义私有模型
- **下游服务**: WPS/KY FastAPI 服务（负责将 Markdown 渲染为 Word）

---

## 完整请求链路（从前端到返回）

```
前端 POST /llm/streaming_llm_socket_with_prompt_list/
  {
    flow_type: "linear",
    raw_msg_list: [...],       // 工作流步骤列表
    chat_history_list: [...],  // 用户上传的文件内容或历史  
    streamingId: "uuid",       // 唯一标识，用于中断
    socket_id: "...",          // WebSocket ID
    generate_file: true,       // 是否最终生成 Word
    return_mode: "socket",     // "socket" 或 "api"
    workflow_name: "报告名称",
  }
        ↓
① routes.js → app.use('/llm', llmRouter)
        ↓
② routes/llm.js → router.post('/streaming_llm_socket_with_prompt_list/', authUser, wfController.streaming_llm_socket_with_prompt_list)
        ↓
③ controller/llm/apis/wf_apis.js → streaming_llm_socket_with_prompt_list()
   判断 flow_type === "linear" → process_linear_process_flow({req, res, controllers, ...})
        ↓
④ controller/llm/_utils/workflow_utils/linear_wf_utils.js → process_linear_process_flow()
   核心 while 循环，依次处理 raw_msg_list 中的每个步骤
        ↓
⑤ 根据 step.type 路由到不同处理器:
   - text/table  → streaming_api_wrapper.streaming_wrapper()
   - pic         → mermaid_utils.mermaid_with_type()
   - pic_code_tool → echart_utils.code_to_chart_main_route()
   - image_generation → doubao_image.ask_doubao_image_generation()
   - rag         → search_db.search_embeddings_and_rerank() + streaming_wrapper()
   - api         → run_agent_api()
   - classifier  → find_skipped_next_index() (条件分支跳转)
   - fixed       → 直接 res.write(raw_msg)
        ↓
⑥ streaming_api_wrapper.js → 根据 model 字段路由到具体大模型 API
   - glm*        → streaming_glm4()
   - deepseek-v3 → streaming_doubao_deepseek_v3()
   - deepseek-r1 → streaming_deepseek_r1()
   - qwen*       → streaming_qwen()
        ↓
⑦ 所有步骤结果追加到 md_result_list (Markdown 富文本链表)
        ↓
⑧ 若 generate_file=true: word.ky_create_docx_file_from_md_list_data_with_pics({md_list})
   → 调用 WPS 微服务，返回 Word 文件 URL
        ↓
⑨ res.write(`<WORD_FILE_URL>...</WORD_FILE_URL>`)
   send_event(AGENT_RECEIVE_FINISHED_FLOW_HISTORY_DATA)
   res.end()
```

---

## 核心数据结构

### 1. 工作流步骤对象 (raw_msg_obj / WorkflowStep)
```javascript
{
  id: "step_uuid",                   // 步骤唯一 ID
  type: "text",                      // 步骤类型（见下方枚举）
  content: "请分析...",               // 发给大模型的 prompt 内容
  title: "## 第一章 分析结果",         // 可选，步骤标题（MD格式）
  model: "deepseek_v3",              // 使用的模型名
  system_prompt: null,               // 可选，系统提示词
  history_mode: "all",               // 历史上下文模式（见下方）
  file_mode: "all",                  // 文件内容模式
  is_thinking_process: false,        // 是否为"思考过程"步骤（不计入最终文档）
  pic_type: "mindMap",              // type=pic 时: mindMap/flowchart/sequenceDiagram
  theme_color: "#4895ef",           // 图表主题色
  enable_thinking: false,           // 是否开启模型思考模式
  datasets: [],                     // type=rag 时: 向量库 ID 列表
  embedding_limit: 5,               // RAG 检索数量
  search_similar_content: null,     // RAG 额外比较内容
  agent_id: null,                   // type=api/rag 时: agent ID
  search_settings: {},              // 搜索引擎设置
  personal_model_id: null,          // 自定义模型 ID
  is_personal: false,               // 是否使用自定义模型
}
```

### 2. 步骤类型枚举 (type)
| type | 说明 |
|------|------|
| `text` | 默认，纯文字，调大模型生成 |
| `table` | 表格模式，注入额外 system_prompt 强制输出 Markdown 表格 |
| [pic](file:///e:/conputer/wrokProject/midlleearth_tax_new/midlleearth_tax_new3/middleearth_tax_new/src/controller/llm/apis/wf_apis.js#226-232) | Mermaid 图（思维导图/流程图/时序图等） |
| `pic_code_tool` | ECharts 代码图表 |
| `image_generation` | 文生图（Doubao 图片生成模型） |
| [rag](file:///e:/conputer/wrokProject/midlleearth_tax_new/midlleearth_tax_new3/middleearth_tax_new/src/controller/llm/apis/wf_apis.js#56-72) | 向量数据库检索增强生成 |
| `api` | 调用外部 API 接口 |
| [classifier](file:///e:/conputer/wrokProject/midlleearth_tax_new/midlleearth_tax_new3/middleearth_tax_new/src/controller/llm/_utils/workflow_utils/linear_wf_utils.js#792-819) | 条件分支判断，决定跳过或继续执行后续步骤 |
| `fixed` | 固定文本，不调大模型，直接输出 content |
| `ai_excel` | AI 操作 Excel 文件（调 KY FastAPI 服务） |

### 3. history_mode 枚举
| history_mode | 说明 |
|------|------|
| [all](file:///e:/conputer/wrokProject/midlleearth_tax_new/midlleearth_tax_new3/middleearth_tax_new/src/controller/llm/apis/wf_apis.js#108-122) | 带全部历史（文件+已处理步骤） |
| `file_history_only` | 只带用户上传的文件内容 |
| `processed_only` | 只带工作流已执行步骤的历史 |
| `none` | 不带任何历史 |

### 4. md_result_list 节点结构（汇总链表）
```javascript
{
  content: "## 分析结果\n\n...",   // 该步骤 Markdown 内容
  type: "text",                   // 或 "pic", "title"
  pic_type: "mindMap",            // type=pic 时有值
  is_thinking_process: false,     // true 时不会计入 Word 文档
  id: "step_uuid",
  info: { level: 2 }              // type=title 时：标题层级
}
```

---

## 关键代码片段

### A. 流式响应设置（防超时核心）
```javascript
// linear_wf_utils.js
res.setHeader('Content-Type', 'text/plain');
res.setHeader('Transfer-Encoding', 'chunked');
res.setHeader('Cache-Control', 'no-cache');
res.setHeader('Connection', 'keep-alive');
```

### B. 主循环（顺序调度）
```javascript
while (i < task_length) {
    let raw_msg_obj = raw_msg_list[i];
    i += 1;

    let { type, content, model, history_mode, ... } = raw_msg_obj;

    // 历史上下文工程
    let history = await get_history_by_mode({ history_mode, processed_history_list, ... });

    // 支持变量替换（{{VAR}} 语法）
    raw_msg = extract_raw_msg_with_var_support({ raw_msg, processed_history_list, predefined_variables });

    // 通知前端"当前在执行哪个步骤"
    send_event({ io, socket_id: socket?.id, res, return_mode,
        message_text: JSON.stringify({ type, id }),
        event_type: "AGENT_START_STEP_NOW" });

    // ↓ type 路由分发 ↓
    if (type === "text" || type === "table") {
        let ans = await streaming_api_wrapper.streaming_wrapper({ prompt: raw_msg, model, ... });
        processed_history_list.push({ role:'user', content: raw_msg, id });
        processed_history_list.push({ role:'assistant', content: ans, id });
        add_step_result_to_final_md_list_result({ history: md_result_list, step_content: ans, ... });
    }
    // ... 其他 type 处理 ...
}
```

### C. 黑盒自纠错 JSON 解析（重试机制）
```javascript
// llm_tools.js - parseToJson()
const parseToJson = async (result) => {
    try {
        // 第一次：尝试提取 ```json 块 并 JSON.parse
        let extracted = result.substring(result.indexOf("```json"), result.lastIndexOf("```"))
        return JSON.parse(extracted.replaceAll("```json",'').replaceAll("\n",'').replaceAll("`",''))
    } catch(e) {
        // 失败：把脏数据发回大模型做数据清洗（黑盒重试）
        let prompt = `从我给你的一段文字中提取出其中JSON的对象，格式是JSON格式，不要返回其他任何信息，我的文字是：${result}`
        let cleaned = await glm4.ask_glm4(prompt)
        return JSON.parse(cleaned.replaceAll("```json",'').replaceAll("\n",'').replaceAll("`",''))
    }
}
```

### D. Word 文件生成（调下游微服务）
```javascript
// 在所有步骤完成后
if (generate_file) {
    let file_url_obj = await word.ky_create_docx_file_from_md_list_data_with_pics({
        user_id,
        md_list: md_result_list,       // 所有步骤结果的 Markdown 链表
        authorization_header: req.headers.authorization
    });
    res.write(`\n<WORD_FILE_URL>${file_url_obj?.result}</WORD_FILE_URL>`);
}
```

### E. 实时推送事件（send_event 统一封装）
```javascript
// 兼容 socket 和 HTTP API 两种模式
const send_event = ({io, socket_id, res, return_mode, message_text, event_type="AGENT_RECEIVE_MESSAGE_IN_PROGRESS", socket_type_postfix=""}) => {
    if (return_mode === "socket") {
        io?.to(socket_id).emit(event_type + socket_type_postfix, { content: message_text });
    } else {
        // api 模式走 SSE
        res?.write(`data: ${message_text}\n\n`);
    }
}
```

---

## 需要实现的文件列表

复刻时需要创建以下文件/模块：

```
src/
├── routes.js                          # 路由注册入口
├── routes/llm.js                     # LLM 相关路由
├── controller/llm/apis/wf_apis.js    # 工作流 Controller 入口
├── controller/llm/_utils/workflow_utils/
│   ├── linear_wf_utils.js            # ⭐ 核心调度主循环
│   ├── flow_wf_utils.js              # 分支流程图工作流（可选）
│   ├── history_context_engineering_utils.js  # 历史上下文管理
│   └── utils.js                      # send_event / send_end_signal 等工具函数
├── controller/llm/llm_model_api/streaming_apis/
│   ├── streaming_api_wrapper.js      # ⭐ 模型路由分发器
│   ├── streaming_llm_models.js       # GLM, Qwen, etc. 实现
│   └── streaming_deepseek.js         # Deepseek 系列实现
├── controller/llm/_utils/
│   ├── llm_tools.js                  # parseToJson / parseToCode / token 管理
│   ├── ky_wps_utils/word.js         # Word 文件生成（调下游微服务）
│   └── picture_utils/
│       ├── mermaid_utils.js          # Mermaid 图生成
│       └── echart/echart_utils.js   # ECharts 图生成
└── utils/
    ├── socket_utils.js               # sendMessageInProgress / sendMessageEnds
    └── socket.js                     # getSocketIO()
```

---

## 最小可行版本实现顺序（建议）

1. **先实现 [streaming_api_wrapper.js](file:///e:/conputer/wrokProject/midlleearth_tax_new/midlleearth_tax_new3/middleearth_tax_new/src/controller/llm/llm_model_api/streaming_apis/streaming_api_wrapper.js)** — 最核心的模型调用封装，支持至少 1 个模型（如 Deepseek）。
2. **实现 [utils.js](file:///e:/conputer/wrokProject/midlleearth_tax_new/midlleearth_tax_new3/middleearth_tax_new/src/controller/user/utils.js) 里的 `send_event`** — 统一推送封装，兼容 socket 和 http。
3. **实现 [linear_wf_utils.js](file:///e:/conputer/wrokProject/midlleearth_tax_new/midlleearth_tax_new3/middleearth_tax_new/src/controller/llm/_utils/workflow_utils/linear_wf_utils.js) 的 [process_linear_process_flow](file:///e:/conputer/wrokProject/midlleearth_tax_new/midlleearth_tax_new3/middleearth_tax_new/src/controller/llm/_utils/workflow_utils/linear_wf_utils.js#43-688)** — 只先支持 `text` 和 `fixed` 两种 type。
4. **实现 [wf_apis.js](file:///e:/conputer/wrokProject/midlleearth_tax_new/midlleearth_tax_new3/middleearth_tax_new/src/controller/llm/apis/wf_apis.js) 的 Controller 入口** — 接入路由，联调成功。
5. **逐步扩展 type**：加 `table` → [pic](file:///e:/conputer/wrokProject/midlleearth_tax_new/midlleearth_tax_new3/middleearth_tax_new/src/controller/llm/apis/wf_apis.js#226-232) (Mermaid) → `pic_code_tool` (ECharts) → [rag](file:///e:/conputer/wrokProject/midlleearth_tax_new/midlleearth_tax_new3/middleearth_tax_new/src/controller/llm/apis/wf_apis.js#56-72)。
6. **最后加 Word 生成**：在所有步骤完成后调用 WPS/FastAPI 微服务。

---

## 前端需要处理的 Socket 事件

| 事件名 | 触发时机 | payload |
|--------|----------|---------|
| `AGENT_RECEIVE_MESSAGE_IN_PROGRESS` | 每个 token 流出时 | `{ content: "..." }` |
| `AGENT_START_STEP_NOW` | 每个步骤开始 | `{ type, id }` |
| `AGENT_RECEIVE_FINISHED_FLOW_HISTORY_DATA` | 全部完成 | `{ history, show_history_md_list, finished_step_id_list }` |
| `WF_AGENT_STARTS` | 工作流开始 | - |
| `WF_AGENT_ENDS` | 工作流结束 | - |
| `personal_file_id_event` | 文件 ID 生成后 | `{ content: file_id }` |

---

## 补充：中断流式请求

前端可随时通过以下接口中断当前工作流：
```
POST /llm/abort_streaming/
{ streamingId: "..." }
```

后端通过 `AbortController.abort()` 终止 fetch 请求，并删除 MongoDB 中对应的进行中文件记录。

---

## 给大模型的开发指令（可直接使用）

> 请根据以上架构文档，使用 Node.js + Express 实现一个 LLM 驱动的多步骤工作流系统。
> 
> **最小可行版本要求**：
> 1. 提供 `POST /llm/streaming_llm_socket_with_prompt_list/` 接口
> 2. 接受 `raw_msg_list`（步骤列表），每个步骤包含 `type`、`content`、[model](file:///e:/conputer/wrokProject/midlleearth_tax_new/midlleearth_tax_new3/middleearth_tax_new/src/controller/llm/apis/wf_apis.js#144-161)、`id` 字段
> 3. 优先支持 `text`、`fixed`、`table` 三种 type
> 4. 使用 HTTP Chunked Transfer 流式返回，每个步骤生成完实时 `res.write()`
> 5. 串行执行步骤（while 循环），上一步的结果追加进 `processed_history_list`，作为下一步的上下文
> 6. 支持对接 Deepseek-V3 API（通过 OpenAI 兼容接口）
>
> **扩展要求（可分阶段实现）**：
> - 支持 [pic](file:///e:/conputer/wrokProject/midlleearth_tax_new/midlleearth_tax_new3/middleearth_tax_new/src/controller/llm/apis/wf_apis.js#226-232) type（调用 Mermaid 生成思维导图代码）
> - 支持 [classifier](file:///e:/conputer/wrokProject/midlleearth_tax_new/midlleearth_tax_new3/middleearth_tax_new/src/controller/llm/_utils/workflow_utils/linear_wf_utils.js#792-819) type（大模型判断执行/跳过后续步骤）
> - 支持 [rag](file:///e:/conputer/wrokProject/midlleearth_tax_new/midlleearth_tax_new3/middleearth_tax_new/src/controller/llm/apis/wf_apis.js#56-72) type（向量数据库检索后再生成）
> - 全部步骤完成后，汇总 `md_result_list`，调用 WPS 微服务生成 Word 文档
> - 支持 `POST /llm/abort_streaming/` 接口中断流式输出

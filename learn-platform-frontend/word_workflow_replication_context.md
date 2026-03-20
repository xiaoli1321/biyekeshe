# LLM 驱动 Word 工作流系统 — 完整复刻技术上下文

> 本文档是专门提供给大模型的技术上下文，请按此文档独立实现 Word 工作流功能。

---

## 1. 系统定位与目标

构建一个 **LLM 驱动的 Word 文档自动生成工作流系统**（基于 React + Redux）。

**核心能力：**
1. 用户配置多步骤 Prompt 工作流（Agent）
2. 上传/选择参考文件作为上下文
3. SSE 流式调用后端 LLM 接口，实时渲染 Markdown 内容
4. 工作流完成后，调用后端接口把 Markdown 内容生成 Word 文件
5. 打开 Word 预览页，支持格式调整、下载 Word / 转换 PDF

---

## 2. 技术栈

| 层级 | 技术 |
|---|---|
| 框架 | React（函数组件 + Hooks）+ React Router v5 |
| 状态管理 | Redux + Redux Thunk |
| HTTP | axios + Fetch API（流式用 Fetch） |
| UI | Material UI v5 (MUI)、Ant Design（message/Spin） |
| Markdown | react-markdown + remark-gfm + remark-math + rehype-katex |
| 代码高亮 | react-syntax-highlighter (dracula 主题) |
| 文件预览 | react-doc-viewer |
| 文件上传 | react-dropzone |
| Token 计算 | gpt-tokenizer |
| 工具库 | micro-dash、uuid、copy-to-clipboard |

---

## 3. 后端接口清单

### 3.1 接口基础地址（来自 [src/configs/constants.js](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/configs/constants.js)）

```js
// 主后端（Node/Python 中间层）
MIDDLE_EARTH_URL = 'http://localhost:3005'  // 生产: 'https://www.dongqianshan.com:3006'

// FastAPI 服务（文件操作）
FAST_API = 'https://kingsyardfastapi.dongqianshan.com'

// KY 服务（旧版接口，部分保留）
KY_URL = 'https://www.dongqianshan.com:8020'
```

所有接口的请求头均需携带：
```
Authorization: token <localStorage.getItem('token')>
Content-Type: application/json
```

---

### 3.2 工作流 Agent 相关

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `{MIDDLE_EARTH_URL}/llm/get_agent_by_id?_id={id}` | 获取 Agent 详情（工作流配置） |
| GET | `{MIDDLE_EARTH_URL}/llm/get_all_workflows/` | 获取工作流列表 |

**Agent 数据结构：**
```json
{
  "_id": "string",
  "name": "工作流名称",
  "flow_type": "linear",         // "linear"=Word流, "linear_excel"=Excel流
  "prompt_list": [
    {
      "id": "uuid",
      "type": "text",            // text | fixed | manual | table | pic
      "content": "prompt内容，支持{{#1}}变量占位符",
      "checked": true
    }
  ],
  "predefined_variables": [
    {
      "name": "变量名",
      "value": "变量值",
      "is_required": true
    }
  ]
}
```

---

### 3.3 文件上传与解析

| 方法 | 路径 | 用途 |
|---|---|---|
| POST | `{MIDDLE_EARTH_URL}/dataset/get_file_content/` | 上传文件并解析内容、计算token |
| GET | `{MIDDLE_EARTH_URL}/user/get_user_fileLists/` | 获取用户个人文件列表 |

---

### 3.4 实体文件（系统文件）

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `{MIDDLE_EARTH_URL}/entity/get_paginated_entities?page=1&pageSize=200&entity_type=all` | 获取一级实体列表 |
| GET | `{MIDDLE_EARTH_URL}/entity/get_entity_file_list?entity_id={id}` | 获取实体下的文件列表 |
| POST | `{MIDDLE_EARTH_URL}/entity/get_file_metadata/` | 获取文件内容详情 |

---

### 3.5 核心：SSE 流式执行接口

#### （1）`linear` 流（Word 生成型）

```
POST {MIDDLE_EARTH_URL}/llm/streaming_llm_socket_with_prompt_list/
```

**请求体：**
```json
{
  "toc_info": null,
  "raw_msg_list": [...],             // prompt_list 的子集（到下一个manual步骤为止）
  "chat_history_list": [             // 文件上下文（每个文件两条消息）
    {"file_name": "xx.pdf", "role": "user", "content": "上传了文件xx.pdf，该文件的内容为：...", "url": ""},
    {"role": "assistant", "content": "好的"}
  ],
  "socket_id": "string",             // WebSocket ID（可为空字符串）
  "streamingId": "uuid",             // 本次流唯一ID
  "file_list": ["xx.pdf"],           // 文件名列表
  "previous_show_history_md_list": [],
  "previous_finished_step_id_list": [],
  "workflow_name": "工作流名称",
  "return_mode": "api",
  "is_test_operation": false,
  "predefined_variables": [{"name": "x", "value": "y"}],
  "prev_processed_history_list": [],
  "personal_generated_file_id": null,
  "existing_personal_generated_file_id": null
}
```

**响应：** SSE 流（Text/Event-Stream），内容通过自定义 XML-like 标签分隔，见第4节。

#### （2）`linear_excel` 流（Excel 生成型）

```
POST {MIDDLE_EARTH_URL}/llm/streaming_llm_socket_with_prompt_list_for_excel/
```

请求体与上相似，额外增加：
```json
{
  "excel_type": "create",
  "shouldStartNewMessage": true
}
```

---

### 3.6 文件生成与格式化

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `{MIDDLE_EARTH_URL}/user/get_excel_personal_files?id={nullFileId}` | 获取后端存储的生成内容 |
| GET | `{MIDDLE_EARTH_URL}/user/get_personal_files?id={nullFileId}` | 获取生成内容（用于关联实体） |
| POST | `{FAST_API}/api/llm_file_ops/create-docx-from-md-list-data-with-pics/` | Markdown列表→Word文件 |
| POST | `{FAST_API}/api/llm_file_ops/change_docx_file_format` | 重新格式化现有Word文件 |
| POST | `{FAST_API}/api/llm_file_ops/get_docx_file_format` | 从模板.docx提取格式设置（multipart/form-data）|
| POST | `{FAST_API}/api/llm_file_ops/convert-word-to-pdf` | Word → PDF（responseType: 'blob'）|

**`create-docx-from-md-list-data-with-pics/` 请求体：**
```json
{
  "cover_info": null,
  "md_list": [                       // showHistoryMdList，每步骤的md内容
    {"id": "uuid", "type": "text", "content": "# 标题\n内容..."}
  ],
  "user_id": "user_id_string",
  "toc_info": null
}
```
**响应：** `{ "result": "https://cdn.xxx.com/files/xxx.docx" }`

**[change_docx_file_format](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/user.js#2515-2537) 请求体：**
```json
{
  "file_url": "https://cdn.xxx.com/files/xxx.docx",
  "format_settings": { ...格式设置对象... }
}
```

---

### 3.7 终止流

| 方法 | 路径 | 用途 |
|---|---|---|
| POST | `{MIDDLE_EARTH_URL}/llm/abort_streaming/` | 通知后端中止（配合前端 AbortController）|

---

## 4. SSE 流协议：[processStreamBuffer](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/data_streaming.js#1-118) 解析器

后端返回的是自定义 XML-like 标签格式的流（不是标准 SSE），前端通过以下函数解析：

### 4.1 协议格式

后端推送的原始数据类似：
```
<AGENT_START_STEP_NOW>{"id":"uuid","type":"text"}</AGENT_START_STEP_NOW>
<think>这是思维过程内容</think>
<md>## 正文标题\n这是正文内容</md>
<WORD_FILE_URL>https://cdn.xxx.com/files/output.docx</WORD_FILE_URL>
<personal_file_id_event>{"content":"file_mongo_id"}</personal_file_id_event>
<AGENT_RECEIVE_FINISHED_FLOW_HISTORY_DATA>{"finished_step_id_list":[...],"show_history_md_list":[...],"history":[...]}</AGENT_RECEIVE_FINISHED_FLOW_HISTORY_DATA>
```

### 4.2 解析器完整代码（[src/actions/data_streaming.js](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/data_streaming.js)）

```js
export function processStreamBuffer(buffer) {
    const tagStartRegex = /<([^>]+)>/;
    let remainingBuffer = buffer;
    const results = [];

    // 纯文本（无标签）直接作为 md 事件
    if (buffer.indexOf('<') === -1 || !/>/.test(buffer)) {
        if (buffer.trim().length > 0) {
            return { remainingBuffer: "", parsedData: { event: "md", content: buffer }, hasCompleteTag: true };
        }
        return { remainingBuffer: "", parsedData: null, hasCompleteTag: false };
    }

    // 处理标签前的纯文本
    const firstTagPos = remainingBuffer.indexOf('<');
    if (firstTagPos > 0) {
        const beforeTag = remainingBuffer.slice(0, firstTagPos);
        if (beforeTag.trim().length > 0) results.push({ event: "md", content: beforeTag });
        remainingBuffer = remainingBuffer.slice(firstTagPos);
    }

    // 循环解析所有完整标签
    while (true) {
        const startMatch = remainingBuffer.match(tagStartRegex);
        if (!startMatch) break;
        const potentialTagName = startMatch[1];
        if (!/^[a-zA-Z_][\w-]*$/.test(potentialTagName)) {
            const textContent = remainingBuffer.slice(0, startMatch.index + startMatch[0].length);
            if (textContent.trim().length > 0) results.push({ event: "md", content: textContent });
            remainingBuffer = remainingBuffer.slice(startMatch.index + startMatch[0].length);
            continue;
        }
        const endTag = `</${startMatch[1]}>`;
        const endIndex = remainingBuffer.indexOf(endTag, startMatch.index);
        if (endIndex === -1) break; // 标签未闭合，等待更多数据
        const contentStart = startMatch.index + startMatch[0].length;
        const content = remainingBuffer.slice(contentStart, endIndex);
        results.push({ event: startMatch[1], content: content });
        remainingBuffer = remainingBuffer.slice(endIndex + endTag.length);
        // 处理标签后的纯文本
        const nextTagPos = remainingBuffer.indexOf('<');
        if (nextTagPos > 0) {
            const afterTag = remainingBuffer.slice(0, nextTagPos);
            if (afterTag.trim().length > 0) results.push({ event: "md", content: afterTag });
            remainingBuffer = remainingBuffer.slice(nextTagPos);
        }
    }

    if (remainingBuffer.trim().length > 0) {
        if (remainingBuffer.indexOf('<') === -1 ||
            (remainingBuffer.indexOf('<') > -1 && remainingBuffer.indexOf('>') === -1)) {
            results.push({ event: "md", content: remainingBuffer });
            remainingBuffer = "";
        }
    }

    return {
        remainingBuffer,
        parsedData: results.length > 0 ? (results.length === 1 ? results[0] : results) : null,
        hasCompleteTag: results.length > 0
    };
}
```

---

### 4.3 事件处理逻辑

| 事件名 | 内容格式 | 处理逻辑 |
|---|---|---|
| `AGENT_START_STEP_NOW` | JSON字符串 `{"id":"uuid","type":"text"}` | 在 `message_lists` 中 push 新条目（初始 content 为空） |
| `think` | 纯文本（思维链） | 追加到当前步骤的 `thinking_process_value` |
| [md](file:///e:/conputer/wrokProject/static_ta3/static_tax/README.md) | Markdown 纯文本 | 追加到当前步骤的 [content](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/user.js#1223-1235) |
| `WORD_FILE_URL` | Word文件CDN URL字符串 | setWordUrl(content)，同时 setIsMessageValue(true) |
| `personal_file_id_event` | JSON字符串 `{"content":"mongo_id"}` | setNullFileId(parsed.content) |
| `personal_file_id_ongoing_event` | JSON字符串 `{"content":"mongo_id"}` | setOngoingId(parsed.content) |
| `AGENT_RECEIVE_FINISHED_FLOW_HISTORY_DATA` | JSON字符串 `{finished_step_id_list, show_history_md_list, history}` | setFinishedStepIdList / setShowHistoryMdList / setHistoryLists |
| `AGENT_RECEIVE_MESSAGE_IN_PROGRESS_THINKING_PROCESS` | 纯文本 | 追加到当前步骤的 thinking_process_value |
| `AGENT_RECEIVE_MESSAGE_IN_PROGRESS_MERMAID_PIC` | Mermaid图表代码 | 追加到 type='pic' 的步骤 content |
| `AGENT_RECEIVE_MESSAGE_IN_PROGRESS_EXCEL_DATA` | JSON字符串（表格数据） | setExcelMessageValue |

---

## 5. 核心 Actions 函数完整代码

### 5.1 [streaming_llm_socket_with_prompt_list](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/user.js#1801-2148)（[src/actions/user.js](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/user.js) 第1801行）

```js
let streamController = null;
export function streaming_llm_socket_with_prompt_list(
    data,
    setAllMessageValue,
    setIsMessageValue,
    setStreamingErrValue,
    setWordUrl,
    setNullFileId,
    setFinishedStepIdList,
    setShowHistoryMdList,
    setHistoryLists,
    setOngoingId
) {
    return (dispatch, getState) => {
        streamController = new AbortController();
        const signal = streamController.signal;
        setIsMessageValue(false);
        setStreamingErrValue('');
        setWordUrl('');
        setNullFileId(null);
        setOngoingId('');
        let accumulatedContent = '';
        let messageLists = [];

        fetch(`${MIDDLE_EARTH_URL}/llm/streaming_llm_socket_with_prompt_list/`, {
            method: 'POST',
            body: JSON.stringify(data),
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'token ' + localStorage.getItem('token')
            },
            signal
        }).then(async res => {
            if (!res.ok) throw new Error(`HTTP错误! 状态码: ${res.status}`);
            const reader = res.body.getReader();
            const textDecoder = new TextDecoder();
            let buffer = '';
            let id = '';
            let type = '';
            try {
                while (true) {
                    const { done, value } = await reader.read();
                    if (done) break;
                    if (signal.aborted) break;
                    const chunk = textDecoder.decode(value, { stream: true });
                    buffer += chunk;
                    const { remainingBuffer, parsedData, hasCompleteTag } = processStreamBuffer(buffer);
                    buffer = remainingBuffer;
                    if (parsedData) {
                        const items = Array.isArray(parsedData) ? parsedData : [parsedData];
                        items.forEach(item => { accumulatedContent += (item.content || ''); });
                        items.forEach(item => {
                            switch (item.event) {
                                case 'AGENT_START_STEP_NOW':
                                    try {
                                        const stepInfo = JSON.parse(item.content);
                                        id = stepInfo.id;
                                        type = stepInfo.type;
                                        setAllMessageValue(prev => {
                                            const tmp = [...prev.message_lists];
                                            const newItem = {
                                                type: type, id: id, content: '',
                                                mermaidId: 'work_merid_' + uuidv4(),
                                            };
                                            if (type === 'text') {
                                                newItem.is_click_hover = false;
                                                newItem.thinking_process_value = '#### 思维过程（不出现在生成的文件中）\n';
                                            }
                                            tmp.push(newItem);
                                            return { ...prev, message_lists: tmp, index: prev.index + 1, content: '' };
                                        });
                                    } catch (e) {}
                                    break;
                                case 'think':
                                    setAllMessageValue(prev => {
                                        let tmp = JSON.parse(JSON.stringify(prev.message_lists));
                                        if (tmp[prev.index] == undefined) {
                                            tmp.push({ type, id, content: '', is_click_hover: false, thinking_process_value: '#### 思维过程（不出现在生成的文件中）\n' + item.content });
                                        } else {
                                            tmp[prev.index].thinking_process_value = item.content;
                                            tmp[prev.index].type = type;
                                        }
                                        return { ...prev, message_lists: tmp };
                                    });
                                    break;
                                case 'md':
                                    setAllMessageValue(prev => {
                                        const tmp = [...prev.message_lists];
                                        if (tmp[prev.index] == undefined) {
                                            tmp.push({ type: type || 'text', id, mermaidId: 'work_merid_' + uuidv4(), content: item.content, is_click_hover: false, thinking_process_value: '#### 思维过程（不出现在生成的文件中）\n' });
                                        } else {
                                            tmp[prev.index].content = (tmp[prev.index].content || '') + item.content;
                                        }
                                        return { ...prev, message_lists: tmp };
                                    });
                                    break;
                                case 'WORD_FILE_URL':
                                    setWordUrl(item.content);
                                    setIsMessageValue(true);
                                    break;
                                case 'personal_file_id_event':
                                    try {
                                        let f = JSON.parse(item.content);
                                        setNullFileId(f.content);
                                    } catch(e) {}
                                    break;
                                case 'personal_file_id_ongoing_event':
                                    try {
                                        let o = JSON.parse(item.content);
                                        setOngoingId(o.content);
                                    } catch(e) {}
                                    break;
                                case 'AGENT_RECEIVE_FINISHED_FLOW_HISTORY_DATA':
                                    try {
                                        let h = JSON.parse(item.content);
                                        setFinishedStepIdList(h.finished_step_id_list);
                                        setShowHistoryMdList(h.show_history_md_list);
                                        setHistoryLists(h.history);
                                    } catch(e) {}
                                    break;
                                default: break;
                            }
                        });
                    }
                }
            } catch (error) {
                if (error.name !== 'AbortError') {
                    setStreamingErrValue(error.message);
                }
            } finally {
                reader.releaseLock();
                setIsMessageValue(true);
            }
        }).catch(error => {
            if (error.name !== 'AbortError') setStreamingErrValue(error.message || '请求处理失败');
            setIsMessageValue(true);
        });
    };
}

export function stopStreamingRequest(data, setIsMessageValue) {
    return (dispatch, getState) => {
        if (streamController) {
            streamController.abort();
            streamController = null;
            // dispatch(abort_streaming({ streamingId: data.streamingId }, ...))
        }
    }
}
```

---

### 5.2 [create_docx_from_md_data](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/user.js#2493-2515)（[src/actions/user.js](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/user.js) 第2493行）

```js
export function create_docx_from_md_data(data, setWordUrl, setIsWork) {
    setWordUrl('');
    setIsWork(true);
    return (dispatch, getState) => {
        axiosInstance.post(`${FAST_API}/api/llm_file_ops/create-docx-from-md-list-data-with-pics/`, data, {
            headers: {
                'content-type': 'application/json',
                'Authorization': 'token ' + localStorage.getItem('token')
            }
        }).then(res => {
            setWordUrl(res.data.result);  // res.data.result = CDN URL
            setIsWork(false);
        }).catch(error => {
            setIsWork(false);
        });
    };
}
```

---

### 5.3 [change_docx_file_format](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/user.js#2515-2537)（第2515行）和 [get_docx_file_format](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/user.js#2537-2557)（第2537行）

```js
export function change_docx_file_format(data, setWordUrl, setIsSettingWork) {
    setWordUrl('');
    setIsSettingWork(false);
    return (dispatch, getState) => {
        axiosInstance.post(`${FAST_API}/api/llm_file_ops/change_docx_file_format`, data, {
            headers: { 'content-type': 'application/json', 'Authorization': 'token ' + localStorage.getItem('token') }
        }).then(res => {
            setWordUrl(res.data.result);
            setIsSettingWork(true);
        });
    };
}

export function get_docx_file_format(data, setFileFormat) {
    setFileFormat(null);
    return (dispatch, getState) => {
        axiosInstance.post(`${FAST_API}/api/llm_file_ops/get_docx_file_format`, data, {
            headers: { 'content-type': 'multipart/form-data', 'Authorization': 'token ' + localStorage.getItem('token') }
        }).then(res => {
            setFileFormat(res.data.result);  // 返回 word_settings 格式的对象
        });
    };
}
```

---

### 5.4 [convert_word_to_pdf](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/user.js#2558-2612)（第2558行）

```js
export function convert_word_to_pdf(data, setIsPdf) {
    return (dispatch, getState) => {
        setIsPdf(true);
        axiosInstance.post(`${FAST_API}/api/llm_file_ops/convert-word-to-pdf`, data, {
            headers: { 'Content-Type': 'application/json', 'Authorization': 'token ' + localStorage.getItem('token') },
            responseType: 'blob',
            timeout: 3600000
        }).then(response => {
            const blob = new Blob([response.data], { type: 'application/pdf' });
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = 'pdf_report_file.pdf';
            link.click();
            window.URL.revokeObjectURL(url);
            setIsPdf(false);
        });
    };
}
```

---

## 6. 前端状态机（React 组件主要 State）

```js
// word_flow_details.js 主要状态

// Agent 信息
const [agent, setAgent] = useState(null);

// 文件相关
const [files, setFiles] = useState([]);           // 原始 File 对象
const [selectedFiles, setSelectedFiles] = useState([]); // 已选中的文件（含content/token）
const [fileLists, setFileLists] = useState([]);   // 解析完成后的文件内容列表

// 流式运行状态
const [allMessageValue, setAllMessageValue] = useState({
    message_lists: [],   // 每个步骤的消息对象数组
    content: '',
    index: 0             // 当前正在更新的步骤索引
});
// message_lists 中每个对象的结构：
// {
//   id: string,          // 对应 prompt_list 中的步骤 ID
//   type: string,        // text | pic | table 等
//   content: string,     // 正文 Markdown
//   thinking_process_value: string,  // 思维链内容
//   is_click_hover: boolean,
//   mermaidId: string    // mermaid 图表唯一 ID
// }

const [isMessageValue, setIsMessageValue] = useState(false);   // 流式是否完成
const [streamingErrValue, setStreamingErrValue] = useState('');

// Word 文件状态
const [wordUrl, setWordUrl] = useState('');        // 最终 Word CDN 地址
const [nullFileId, setNullFileId] = useState(''); // 后端存储的文件 Mongo ID

// 历史记录（多轮支持）
const [finishedStepIdList, setFinishedStepIdList] = useState([]);
const [showHistoryMdList, setShowHistoryMdList] = useState([]);
const [historyLists, setHistoryLists] = useState([]);

// 流 ID
const [streamingId, setStreamingId] = useState('');
const [ongoingId, setOngoingId] = useState('');
const [lastRunLastId, setLastRunLastId] = useState('');  // 最后一步的 ID

// 人工判断步骤
const [pendingManualContent, setPendingManualContent] = useState('');

// 生成并查看文件
const [personalValue, setPersonalValue] = useState([]);  // get_excel_personal_files 结果
const [isWork, setIsWork] = useState(false);             // create_docx 加载中

// UI
const [runDrawerOpen, setRunDrawerOpen] = useState(false);  // 结果 Drawer 开启
const [tabsValue, setTabsValue] = useState(0);              // 文件选择 Tab（0=个人文件,1=系统文件）
```

---

## 7. 工作流运行核心逻辑（[workFlowAffirm](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/components/web/chat/word_flow_details.js#657-759)）

```js
const workFlowAffirm = () => {
    //① 构建 chat_history_list（文件上下文）
    const chat_history_list = [];
    const file_list = [];
    for (let f of selectedFiles) {
        const fname = f?.name || f?.file_name;
        const fcontent = f?.content || '';
        chat_history_list.push({ file_name: fname, role: 'user', content: `上传了文件${fname}，该文件的内容为：${fcontent}`, url: '' });
        chat_history_list.push({ role: 'assistant', content: '好的' });
        file_list.push(fname);
    }

    //② 处理变量（将 <span data-variable="{{#1}}">...</span> → {{#1}}）
    const convertVariables = (content) => {
        if (typeof content !== 'string') return content;
        return content.replace(/<span[^>]*data-variable="(\{\{#\d+\}\})"[^>]*>[\s\S]*?<\/span>/g, '$1');
    };
    const processed_prompt_list = (agent?.prompt_list || []).map(item => {
        const copy = { ...item };
        if (typeof copy.content === 'string') copy.content = convertVariables(copy.content);
        return copy;
    });

    //③ 截取到下一个 manual 步骤（不含）
    const nextManualIndex = processed_prompt_list.findIndex(v => v.type === 'manual' || v.type === '人工判断');
    const send_prompt_list = processed_prompt_list.slice(0, nextManualIndex !== -1 ? nextManualIndex : processed_prompt_list.length);

    //④ 记录最后一个步骤 ID
    const _last = send_prompt_list[send_prompt_list.length - 1];
    setLastRunLastId(_last?.id || '');

    //⑤ 生成 streamingId，发起流请求
    const id = uuidv4();
    setStreamingId(id);
    setRunDrawerOpen(true);

    dispatch(streaming_llm_socket_with_prompt_list({
        toc_info: null,
        raw_msg_list: send_prompt_list,
        chat_history_list,
        socket_id: socket_id,
        streamingId: id,
        file_list,
        previous_show_history_md_list: [],
        previous_finished_step_id_list: [],
        workflow_name: agent?.name || '',
        return_mode: 'api',
        is_test_operation: false,
        predefined_variables: [],
        personal_generated_file_id: null,
        prev_processed_history_list: [],
        existing_personal_generated_file_id: null,
    },
        v => setAllMessageValue(v),
        v => setIsMessageValue(v),
        v => setStreamingErrValue(v),
        v => setWordUrl(v),
        v => setNullFileId(v),
        v => setFinishedStepIdList(v),
        v => setShowHistoryMdList(v),
        v => setHistoryLists(v),
        v => setOngoingId(v)
    ));
};
```

---

## 8. 查看文件 → 跳转 Word 预览页

```js
// 1. 触发：用户点击"查看文件"
const create_word = () => {
    dispatch(get_excel_personal_files({ id: nullFileId }, v => setPersonalValue(v)));
};

// 2. get_excel_personal_files 拿到数据后，调用 create_docx_from_md_data
useEffect(() => {
    if (personalValue.length > 0) {
        dispatch(create_docx_from_md_data({
            cover_info: null,
            md_list: showHistoryMdList,
            user_id: user_id,
            toc_info: null
        }, v => setWordUrl(v), v => setIsWork(v)));
    }
}, [personalValue]);

// 3. wordUrl 变化后，打开预览页
useEffect(() => {
    if (wordUrl !== '') {
        window.open(`${window.location.origin}/report/word_html?url=${wordUrl}`);
    }
}, [wordUrl]);
```

---

## 9. Word 格式设置对象（`word_settings`）

```js
const defaultWordSettings = {
    paragraph: {
        font: { name: '仿宋', size: '16' },
        line_spacing: '1.5',
        space_after: '8',
        first_line_indent: '32'
    },
    title: {
        h1: { alignment: 'CENTER', italic: false, bold: false, prefix_style: '一、', font: { name: '方正小标宋简体', size: '22' }, space_before: '32', space_after: '16' },
        h2: { alignment: 'LEFT',   italic: false, bold: false, prefix_style: '（一）', font: { name: '黑体', size: '16' }, space_before: '32', space_after: '16' },
        h3: { alignment: 'LEFT',   italic: false, bold: false, prefix_style: '1．', font: { name: '楷体', size: '16' }, space_before: '32', space_after: '16' },
        h4: { alignment: 'LEFT',   italic: false, bold: false, prefix_style: '（1）', font: { name: '仿宋', size: '16' }, space_before: '32', space_after: '16' },
        h5: { alignment: 'LEFT',   italic: false, bold: false, prefix_style: '①', font: { name: '仿宋', size: '16' }, space_before: '32', space_after: '16' }
    },
    picture: { alignment: 'CENTER', size: 1.0 },
    table: {
        alignment: 'CENTER',
        table_header: { background_color: null, font: { name: '仿宋', size: '14', color: '000000' }, alignment: 'LEFT', italic: false, bold: false },
        table_content: { background_color: null, font: { name: '仿宋', size: '14', color: '000000' }, alignment: 'LEFT', italic: false, bold: false },
        table_border: { border_size: '8', border_style: 'single', border_color: '000000' }
    }
};
```

---

## 10. 路由注册（[src/App.js](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/App.js)）

```js
// 需要注册的路由
<Route path='/report/word_flow_details' component={WordFlowDetails} />
<Route path='/report/word_html' component={WordHtml} />
// 也可在 largemodel 路径下
<Route path='/largemodel/word_html' component={WordHtml} />
```

---

## 11. 人工判断（manual 步骤）续跑逻辑

当 [prompt_list](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/user.js#1801-2148) 中存在 `type === 'manual'` 的步骤时，工作流会在该步骤前暂停，等待用户输入，然后继续执行剩余步骤：

```js
const manual_confirm = () => {
    // 找到当前最后运行步骤的下一步（应为 manual 类型）
    const lists = agent?.prompt_list || [];
    const lastIdx = lists.findIndex(item => item?.id === lastRunLastId);
    const nextStep = lists[lastIdx + 1];
    if (!nextStep || nextStep.type !== 'manual') return;

    // 将用户输入拼入下一批 prompt_list 的第一条
    const content = pendingManualContent.trim();
    const startIdx = lastIdx + 2; // manual 之后的步骤起点
    // 找到再下一个 manual 步骤
    const nextManualIndex = lists.findIndex((v, i) => i > startIdx && (v.type === 'manual'));
    const endExclusive = nextManualIndex !== -1 ? nextManualIndex : lists.length;
    const raw_msg_list = lists.slice(startIdx, endExclusive);
    // 如果第一条 content 为空，填入用户输入
    if (raw_msg_list.length > 0 && !raw_msg_list[0].content?.trim()) {
        raw_msg_list[0] = { ...raw_msg_list[0], content };
    }

    // 继续调用流式接口
    dispatch(streaming_llm_socket_with_prompt_list({ raw_msg_list, ... }, ...callbacks));
};
```

---

## 12. 分阶段实现路线图

### MVP 第一阶段（可全流程跑通）
1. [processStreamBuffer](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/data_streaming.js#1-118) 解析器（直接复用上文代码）
2. [streaming_llm_socket_with_prompt_list](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/user.js#1801-2148) Action（处理 [md](file:///e:/conputer/wrokProject/static_ta3/static_tax/README.md)、`WORD_FILE_URL`、`personal_file_id_event` 3个事件即可）
3. 基础 UI：Agent 信息展示 + 文件上传 + 运行按钮 + Markdown 实时渲染
4. [create_docx_from_md_data](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/user.js#2493-2515) → 打开 word_html 预览页

### 第二阶段（完整功能）
5. `AGENT_START_STEP_NOW` / `think` / `AGENT_RECEIVE_FINISHED_FLOW_HISTORY_DATA` 事件处理
6. 人工判断步骤（manual）续跑
7. 系统文件（实体）选择
8. 停止流（AbortController）

### 第三阶段（格式化功能）
9. Word 格式设置面板（[word_html.js](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/components/web/chat/word_html.js) 全部功能）
10. 上传模板 [get_docx_file_format](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/user.js#2537-2557) + 重新格式化 [change_docx_file_format](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/user.js#2515-2537)
11. 转 PDF [convert_word_to_pdf](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/user.js#2558-2612)

---

## 13. 涉及文件绝对路径

| 文件 | 绝对路径 |
|---|---|
| 工作流详情主界面 | [e:\conputer\wrokProject\static_ta3\static_tax\src\components\web\chat\word_flow_details.js](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/components/web/chat/word_flow_details.js) |
| Word 预览/格式设置页 | [e:\conputer\wrokProject\static_ta3\static_tax\src\components\web\chat\word_html.js](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/components/web/chat/word_html.js) |
| 流式&Word生成核心 Actions | [e:\conputer\wrokProject\static_ta3\static_tax\src\actions\user.js](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/user.js) |
| SSE 流解析器 | [e:\conputer\wrokProject\static_ta3\static_tax\src\actions\data_streaming.js](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/data_streaming.js) |
| LLM/Agent 辅助 Actions | [e:\conputer\wrokProject\static_ta3\static_tax\src\actions\llm.js](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/llm.js) |
| 接口地址常量 | [e:\conputer\wrokProject\static_ta3\static_tax\src\configs\constants.js](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/configs/constants.js) |
| 路由注册 | [e:\conputer\wrokProject\static_ta3\static_tax\src\App.js](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/App.js) |
| Redux Action 类型 | [e:\conputer\wrokProject\static_ta3\static_tax\src\actions\types.js](file:///e:/conputer/wrokProject/static_ta3/static_tax/src/actions/types.js) |

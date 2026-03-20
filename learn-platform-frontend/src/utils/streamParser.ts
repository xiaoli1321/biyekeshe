/**
 * SSE 流式缓冲区解析器
 * 等价于原 React 项目的 processStreamBuffer()
 *
 * 后端 Java 版推送的格式是 JSON SSE + 纯文本 Markdown 混合流：
 *   data: {"type":"AGENT_START_STEP_NOW","id":"step1"}\n\n
 *   AI生成的token token token\n\n
 *   data: {"type":"WF_AGENT_ENDS"}\n\n
 */

export interface StreamEvent {
  event: string   // 事件类型: 'data_event' | 'text' | 'WF_AGENT_STARTS' 等
  content: string // 事件内容
  payload?: any   // 解析后的 JSON（如果 content 是 JSON 字符串）
}

export interface ParseResult {
  events: StreamEvent[]
  remaining: string
}

/**
 * 解析流式缓冲区
 * 支持两种格式：
 * 1. SSE 标准格式: "data: {...}\n\n"
 * 2. 纯文本 token（逐字 LLM 输出）
 */
export function parseStreamBuffer(buffer: string): ParseResult {
  const events: StreamEvent[] = []
  let remaining = buffer

  // 按行处理
  const lines = remaining.split('\n')
  remaining = '' // 清空，未完整的行放回 remaining

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]

    if (line.startsWith('data: ')) {
      // SSE 事件行
      const jsonStr = line.slice(6).trim()
      if (!jsonStr || jsonStr === '[DONE]') continue

      try {
        const parsed = JSON.parse(jsonStr)
        const eventType = parsed.type || 'unknown'
        const content = parsed.content || ''

        // 尝试嵌套 JSON 解析（如 AGENT_START_STEP_NOW 的 content 也是 JSON 字符串）
        let payload: any = null
        if (content) {
          try { payload = JSON.parse(content) } catch {}
        } else {
          payload = parsed
        }

        events.push({ event: eventType, content, payload })
      } catch {
        // JSON 解析失败，当作 token 文本
        const text = line.slice(6)
        if (text.trim()) {
          events.push({ event: 'token', content: text, payload: null })
        }
      }
    } else if (line.trim() === '') {
      // 空行（SSE 分隔符），忽略
      continue
    } else {
      // 纯文本 token（LLM 直接 write 出来的内容）
      if (line) {
        events.push({ event: 'token', content: line, payload: null })
      }
    }
  }

  return { events, remaining }
}

package com.learnplatform.service.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnplatform.dto.workflow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 线性工作流核心调度主循环
 * 等价于原 Node.js 的 linear_wf_utils.js → process_linear_process_flow()
 *
 * 执行流程：
 *  1. 设置 Chunked 流式响应头
 *  2. while 循环遍历 rawMsgList
 *  3. 根据 step.type 路由处理器
 *  4. 每步结果追加到 processedHistoryList（上下文工程）
 *  5. 汇总到 mdResultList（最终可生成 Word）
 *  6. 全部完成后发送结束事件
 *
 * MVP 支持类型：text / fixed
 * 扩展类型：table（注入 table system_prompt）
 */
@Service
public class LinearWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(LinearWorkflowService.class);

    /** SSE 事件前缀，区分控制事件和正文内容 */
    private static final String EVENT_PREFIX = "data: ";

    /** 变量替换正则：{{VAR_1}} 替换为第 N-1 步骤结果 */
    private static final Pattern VAR_PATTERN = Pattern.compile("\\{\\{VAR_(\\d+)\\}\\}");

    @Autowired
    private LlmStreamingService llmStreamingService;

    @Autowired
    private WorkflowAbortService abortService;

    @Autowired
    private ObjectMapper objectMapper;

    /** table type 注入的额外 system prompt（强制模型输出 Markdown 表格） */
    private static final String TABLE_SYSTEM_PROMPT_SUFFIX =
            "\n\n# 输出格式要求\n请必须使用 Markdown 表格格式（|列1|列2|）输出结果，不要输出其他格式，只输出表格。";

    /**
     * 执行线性工作流主方法
     *
     * @param request  工作流请求（步骤列表、历史、配置）
     * @param response HTTP 响应对象（用于流式写出）
     */
    public void processLinearFlow(WorkflowRequest request, HttpServletResponse response) {
        // ===== 1. 设置流式响应头（等价于 Node.js 的 res.setHeader）=====
        response.setContentType("text/event-stream;charset=UTF-8");
        response.setHeader("Transfer-Encoding", "chunked");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no"); // 禁用 Nginx 缓冲

        PrintWriter writer;
        try {
            writer = response.getWriter();
        } catch (Exception e) {
            log.error("Failed to get response writer", e);
            return;
        }

        List<WorkflowStep> rawMsgList = request.getRawMsgList();
        if (rawMsgList == null || rawMsgList.isEmpty()) {
            sendEvent(writer, "WF_AGENT_ENDS", null);
            writer.flush();
            return;
        }

        String streamingId = request.getStreamingId();

        // ===== 2. 注册中止标志 =====
        AtomicBoolean abortFlag = abortService.register(streamingId);

        // 已处理步骤的历史列表（上下文工程核心）
        List<HistoryMessage> processedHistoryList = new ArrayList<>();

        // 用户上传的文件历史（初始化）
        List<HistoryMessage> fileHistoryList = new ArrayList<>();
        if (request.getChatHistoryList() != null) {
            for (HistoryMessage msg : request.getChatHistoryList()) {
                msg.setIsFileHistory(true);
                fileHistoryList.add(msg);
            }
        }

        // 汇总结果链表
        List<MdResultNode> mdResultList = new ArrayList<>();

        // 已完成步骤 ID 列表（用于最终 payload）
        List<String> finishedStepIds = new ArrayList<>();

        // 发送工作流开始事件
        sendEvent(writer, "WF_AGENT_STARTS", null);
        writer.flush();

        // ===== 3. 核心 while 循环（等价原 JS while (i < task_length)）=====
        int i = 0;
        int taskLength = rawMsgList.size();

        while (i < taskLength) {
            // 检查中止标志
            if (abortFlag.get()) {
                log.info("Workflow aborted at step {}, streamingId: {}", i, streamingId);
                sendEvent(writer, "WF_AGENT_ABORTED", null);
                break;
            }

            WorkflowStep step = rawMsgList.get(i);
            i += 1;

            String stepId = step.getId() != null ? step.getId() : "step_" + i;
            String type = step.getType();
            String historyMode = step.getHistoryMode();
            String rawContent = step.getContent() != null ? step.getContent() : "";

            log.info("Processing step [{}/{}] id={} type={}", i, taskLength, stepId, type);

            // ===== 4. 变量替换（{{VAR_N}} 语法）=====
            rawContent = replaceVariables(rawContent, processedHistoryList, request.getPredefinedVariables());

            // ===== 5. 通知前端当前在执行哪个步骤（AGENT_START_STEP_NOW）=====
            try {
                String stepStartPayload = objectMapper.writeValueAsString(
                        Map.of("type", type, "id", stepId)
                );
                sendEvent(writer, "AGENT_START_STEP_NOW", stepStartPayload);
                writer.flush();
            } catch (Exception e) {
                log.warn("Failed to serialize step start event", e);
            }

            // ===== 6. 输出步骤标题（如果有）=====
            if (step.getTitle() != null && !step.getTitle().isBlank()) {
                sendEvent(writer, "token", step.getTitle() + "\n\n");
            }

            // ===== 7. type 路由分发（Strategy Pattern）=====
            String stepResult = "";

            switch (type) {

                case "fixed":
                    // fixed type：直接输出 content，不调大模型
                    stepResult = rawContent;
                    sendEvent(writer, "token", rawContent);
                    break;

                case "text":
                    // text type：调大模型生成内容，流式输出
                    List<HistoryMessage> textHistory = getHistoryByMode(
                            historyMode, processedHistoryList, fileHistoryList);
                    stepResult = llmStreamingService.streamingWrapper(
                            rawContent,
                            step.getModel(),
                            textHistory,
                            step.getSystemPrompt(),
                            writer,
                            abortFlag
                    );
                    break;

                case "table":
                    // table type：注入额外 system prompt 强制输出 Markdown 表格
                    List<HistoryMessage> tableHistory = getHistoryByMode(
                            historyMode, processedHistoryList, fileHistoryList);
                    String tableSystemPrompt = (step.getSystemPrompt() != null ? step.getSystemPrompt() : "")
                            + TABLE_SYSTEM_PROMPT_SUFFIX;
                    stepResult = llmStreamingService.streamingWrapper(
                            rawContent,
                            step.getModel(),
                            tableHistory,
                            tableSystemPrompt,
                            writer,
                            abortFlag
                    );
                    break;

                default:
                    // 暂不支持的 type（pic / rag / classifier 等），输出占位提示
                    log.warn("Unsupported step type '{}' for step {}, skipping", type, stepId);
                    stepResult = "[步骤类型 " + type + " 暂不支持]\n";
                    sendEvent(writer, "token", stepResult);
                    break;
            }

            // ===== 8. 将步骤结果追加到 processedHistoryList（上下文工程）=====
            // is_thinking_process=true 的步骤仍会追加到历史（可用于后续步骤引用），但不计入 Word 文档
            if (stepResult != null && !stepResult.isEmpty()) {
                processedHistoryList.add(new HistoryMessage("user", rawContent, stepId));
                processedHistoryList.add(new HistoryMessage("assistant", stepResult, stepId));
            }

            // ===== 9. 追加到汇总链表（is_thinking_process=true 时标记）=====
            MdResultNode resultNode = new MdResultNode(
                    (step.getTitle() != null ? step.getTitle() + "\n\n" : "") + stepResult,
                    "fixed".equals(type) ? "fixed" : "text",
                    stepId
            );
            resultNode.setIsThinkingProcess(step.getIsThinkingProcess());
            mdResultList.add(resultNode);

            finishedStepIds.add(stepId);

            // 步骤间换行（确保 Markdown 段落分隔）
            sendEvent(writer, "token", "\n\n");
        }

        // ===== 10. 所有步骤完成，发送结束事件 =====
        try {
            // 构造最终历史 payload（仅供前端记录，不做云端持久化）
            List<Map<String, Object>> showHistoryMdList = new ArrayList<>();
            for (MdResultNode node : mdResultList) {
                if (!Boolean.TRUE.equals(node.getIsThinkingProcess())) {
                    showHistoryMdList.add(Map.of(
                            "content", node.getContent(),
                            "type", node.getType(),
                            "id", node.getStepId() != null ? node.getStepId() : ""
                    ));
                }
            }

            String finishPayload = objectMapper.writeValueAsString(Map.of(
                    "history", processedHistoryList.stream()
                            .map(h -> Map.of("role", h.getRole(), "content", h.getContent()))
                            .toList(),
                    "show_history_md_list", showHistoryMdList,
                    "finished_step_id_list", finishedStepIds
            ));
            sendEvent(writer, "AGENT_RECEIVE_FINISHED_FLOW_HISTORY_DATA", finishPayload);
        } catch (Exception e) {
            log.warn("Failed to send finish event", e);
        }

        sendEvent(writer, "WF_AGENT_ENDS", null);
        writer.flush();

        // ===== 11. 清理中止标志 =====
        abortService.cleanup(streamingId);

        log.info("Workflow completed, streamingId: {}, total steps: {}, finished: {}",
                streamingId, taskLength, finishedStepIds.size());
    }

    // ==================== 私有工具方法 ====================

    /**
     * 根据 historyMode 获取合并后的历史列表
     * 等价于原 JS get_history_by_mode()
     *
     * @param historyMode        all / none / file_history_only / processed_only
     * @param processedHistory   已处理步骤的历史
     * @param fileHistory        用户上传的文件历史
     */
    private List<HistoryMessage> getHistoryByMode(
            String historyMode,
            List<HistoryMessage> processedHistory,
            List<HistoryMessage> fileHistory
    ) {
        if (historyMode == null) historyMode = "all";

        return switch (historyMode) {
            case "none" -> Collections.emptyList();
            case "file_history_only" -> new ArrayList<>(fileHistory);
            case "processed_only" -> new ArrayList<>(processedHistory);
            case "all" -> {
                List<HistoryMessage> combined = new ArrayList<>();
                combined.addAll(fileHistory);
                combined.addAll(processedHistory);
                yield combined;
            }
            default -> new ArrayList<>(processedHistory);
        };
    }

    /**
     * 变量替换：将 {{VAR_1}} 替换为第 1 个步骤的 assistant 回复
     * 等价于原 JS extract_raw_msg_with_var_support()
     *
     * @param content            原始 prompt 内容（可能包含 {{VAR_N}} 占位符）
     * @param processedHistory   已处理历史（user + assistant 交替）
     * @param predefined         预定义变量 map（可为 null）
     */
    private String replaceVariables(
            String content,
            List<HistoryMessage> processedHistory,
            Map<String, String> predefined
    ) {
        if (content == null || !content.contains("{{")) {
            return content;
        }

        // 先处理预定义变量
        if (predefined != null) {
            for (Map.Entry<String, String> entry : predefined.entrySet()) {
                content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
        }

        // 处理 {{VAR_N}} — 取第 N 个步骤的 assistant 输出
        // processedHistory 结构：[user_0, assistant_0, user_1, assistant_1, ...]
        // VAR_1 对应 processedHistory[1]（index 1 = 第 1 步 assistant 回复）
        Matcher matcher = VAR_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            int varIndex = Integer.parseInt(matcher.group(1));
            // assistant 结果在奇数位置（0-based）：VAR_1=index 1, VAR_2=index 3
            int historyIndex = (varIndex - 1) * 2 + 1;
            String replacement = "";
            if (historyIndex < processedHistory.size()) {
                replacement = processedHistory.get(historyIndex).getContent();
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 向 HTTP 流式响应写入 SSE 控制事件
     * 等价于原 JS send_event()（api 模式）
     *
     * 格式（与前端约定）：
     *   data: {"type":"EVENT_NAME","content":"...payload..."}\n\n
     */
    private void sendEvent(PrintWriter writer, String eventType, String payload) {
        try {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("type", eventType);
            if (payload != null) {
                event.put("content", payload);
            }
            writer.write(EVENT_PREFIX + objectMapper.writeValueAsString(event) + "\n\n");
            writer.flush();
        } catch (Exception e) {
            log.warn("Failed to send event {}: {}", eventType, e.getMessage());
        }
    }
}

package com.learnplatform.controller.api;

import com.learnplatform.dto.workflow.AbortRequest;
import com.learnplatform.dto.workflow.WorkflowRequest;
import com.learnplatform.service.workflow.LinearWorkflowService;
import com.learnplatform.service.workflow.WorkflowAbortService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * LLM 工作流 Controller
 * 等价于原 Node.js 的 routes/llm.js + controller/llm/apis/wf_apis.js
 *
 * 路由：
 *   POST /api/llm/streaming_workflow     — 启动工作流（流式 Chunked 返回）
 *   POST /api/llm/abort_streaming        — 中断工作流
 */
@RestController
@RequestMapping("/api/llm")
@Tag(name = "LLM Workflow", description = "LLM 驱动的多步骤 Word 工作流接口")
public class WorkflowController {

    private static final Logger log = LoggerFactory.getLogger(WorkflowController.class);

    @Autowired
    private LinearWorkflowService linearWorkflowService;

    @Autowired
    private WorkflowAbortService workflowAbortService;

    /**
     * 启动线性工作流（HTTP Chunked 流式传输）
     *
     * 等价于原 Node.js：
     *   POST /llm/streaming_llm_socket_with_prompt_list/
     *
     * 前端使用 fetch + ReadableStream 或 EventSource 接收分块响应。
     * 每步骤开始时会先推送控制事件（以 "data: " 前缀区分），
     * 随后紧跟该步骤的 Markdown 内容（逐 token 流出）。
     *
     * @param request  工作流请求体（步骤列表、配置）
     * @param response HTTP 响应（直接写入流）
     */
    @PostMapping("/streaming_workflow")
    @Operation(summary = "启动 LLM 工作流（流式）", description = "接收步骤列表，按顺序调用 LLM，实时 chunked 流返回")
    public void streamingWorkflow(
            @RequestBody WorkflowRequest request,
            HttpServletResponse response
    ) {
        log.info("Received workflow request: workflowName={}, steps={}, streamingId={}",
                request.getWorkflowName(),
                request.getRawMsgList() != null ? request.getRawMsgList().size() : 0,
                request.getStreamingId());

        // 校验基本参数
        if (request.getRawMsgList() == null || request.getRawMsgList().isEmpty()) {
            try {
                response.setStatus(400);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"rawMsgList 不能为空\"}");
            } catch (Exception e) {
                log.error("Error writing 400 response", e);
            }
            return;
        }

        // 生成 streamingId（如果前端没传）
        if (request.getStreamingId() == null || request.getStreamingId().isBlank()) {
            request.setStreamingId(java.util.UUID.randomUUID().toString());
        }

        // 设置响应头部信息，避免 Nginx 或网关缓存导致不流式输出
        response.setStatus(200);
        response.setContentType("text/event-stream;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        linearWorkflowService.processLinearFlow(request, response);
    }

    /**
     * 中断流式工作流
     * 等价于原 Node.js：POST /llm/abort_streaming/
     *
     * @param abortRequest 包含 streamingId
     */
    @PostMapping("/abort_streaming")
    @Operation(summary = "中断工作流", description = "通过 streamingId 中断正在进行的工作流")
    public ResponseEntity<Map<String, Object>> abortStreaming(
            @RequestBody AbortRequest abortRequest
    ) {
        String streamingId = abortRequest.getStreamingId();
        log.info("Aborting streaming workflow: streamingId={}", streamingId);

        if (streamingId == null || streamingId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "streamingId 不能为空"
            ));
        }

        boolean aborted = workflowAbortService.abort(streamingId);
        return ResponseEntity.ok(Map.of(
                "success", aborted,
                "message", aborted ? "工作流已发送中止信号" : "未找到该 streamingId 的运行中工作流"
        ));
    }
}

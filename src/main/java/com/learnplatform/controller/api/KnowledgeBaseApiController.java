package com.learnplatform.controller.api;

import com.learnplatform.dto.ApiResponse;
import com.learnplatform.dto.request.KbChatRequest;
import com.learnplatform.entity.KbChunk;
import com.learnplatform.entity.KbDocument;
import com.learnplatform.security.UserPrincipal;
import com.learnplatform.service.KbCollectionService;
import com.learnplatform.service.KnowledgeBaseService;
import com.learnplatform.service.workflow.LlmStreamingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/kb")
@Tag(name = "知识库管理", description = "处理文档录入、检索及RAG问答接口")
@CrossOrigin(origins = "*", maxAge = 3600)
public class KnowledgeBaseApiController {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseApiController.class);

    @Autowired
    private KnowledgeBaseService kbService;

    @Autowired
    private KbCollectionService collectionService;

    @Autowired
    private LlmStreamingService llmStreamingService;

    @Operation(summary = "上传文档", description = "上传 Word/PDF/TXT 自动分块入库，需指定 collectionId")
    @PostMapping("/documents/upload")
    public ApiResponse<KbDocument> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("collectionId") String collectionId,
            Authentication authentication
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if (!collectionService.existsAndBelongsToUser(collectionId, userPrincipal.getId())) {
            return ApiResponse.error("无权操作此知识库或知识库不存在", "FORBIDDEN");
        }
        try {
            KbDocument doc = kbService.processUpload(file, collectionId);
            return ApiResponse.success(doc);
        } catch (Exception e) {
            log.error("Upload failed", e);
            return ApiResponse.error("上传解析失败: " + e.getMessage());
        }
    }

    @Operation(summary = "文本录入", description = "手动录入长文本或知识条目")
    @PostMapping("/documents/text")
    public ApiResponse<KbDocument> addTextEntry(
            @RequestBody Map<String, String> payload,
            Authentication authentication
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String collectionId = payload.get("collectionId");
        if (collectionId == null || !collectionService.existsAndBelongsToUser(collectionId, userPrincipal.getId())) {
            return ApiResponse.error("无权操作此知识库或知识库不存在", "FORBIDDEN");
        }

        String title = payload.getOrDefault("title", "手动录入-" + System.currentTimeMillis());
        String content = payload.get("content");
        if (content == null || content.isBlank()) {
            return ApiResponse.error("内容不能为空");
        }
        KbDocument doc = kbService.processTextEntry(title, content, collectionId);
        return ApiResponse.success(doc);
    }

    @Operation(summary = "获取知识库下的文档列表")
    @GetMapping("/documents")
    public ApiResponse<List<KbDocument>> listDocuments(
            @RequestParam("collectionId") String collectionId,
            Authentication authentication
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if (!collectionService.existsAndBelongsToUser(collectionId, userPrincipal.getId())) {
            return ApiResponse.error("无权访问", "FORBIDDEN");
        }
        return ApiResponse.success(kbService.getDocumentsByCollection(collectionId));
    }

    @Operation(summary = "获取文档的具体分块（用于前端显示原始数据）")
    @GetMapping("/documents/{docId}/chunks")
    public ApiResponse<List<KbChunk>> getDocumentChunks(
            @PathVariable("docId") String docId,
            Authentication authentication
    ) {
        // TODO: 严格校验 docId 属于当前用户的 collection
        return ApiResponse.success(kbService.getChunksByDocument(docId));
    }

    @Operation(summary = "删除文档")
    @DeleteMapping("/documents/{id}")
    public ApiResponse<Void> deleteDocument(@PathVariable("id") String id, Authentication authentication) {
        // TODO: 校验所有权
        kbService.deleteDocument(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "知识库问答 (RAG)", description = "基于已入库的内容进行混合检索并回答")
    @PostMapping("/chat")
    public void chat(
            @RequestBody KbChatRequest request,
            Authentication authentication,
            HttpServletResponse response
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if (request.getCollectionId() == null || !collectionService.existsAndBelongsToUser(request.getCollectionId(), userPrincipal.getId())) {
            response.setStatus(403);
            return;
        }

        log.info("KB RAG Chat request received: {}", request.getMessage());

        // 1. 混合检索召回核心上下文
        List<KbChunk> topChunks = kbService.hybridSearch(request.getMessage(), request.getCollectionId(), 5);
        
        String context = topChunks.stream()
                .map(KbChunk::getContent)
                .collect(Collectors.joining("\n---\n"));

        // 2. 构建系统提示词
        String systemPrompt = String.format(
                "你是一个专业的知识库问答助手。请基于以下提供的参考资料回答用户的问题。\n" +
                "如果参考资料中没有相关信息，请明确告知用户，不要编造答案。\n\n" +
                "### 参考资料：\n" +
                "%s\n" +
                "### 回答要求：\n" +
                "1. 语言简洁专业。\n" +
                "2. 如果引用了特定资料，请在回答末尾注明。",
                context.isEmpty() ? "（未找到相关参考资料）" : context
        );

        log.info("System Prompt length: {}", systemPrompt.length());
        log.debug("Full System Prompt:\n{}", systemPrompt);

        // 3. 配置 SSE 响应头
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        try (PrintWriter writer = response.getWriter()) {
            AtomicBoolean abortFlag = new AtomicBoolean(false);
            
            // 4. 调用大模型流式生成
            llmStreamingService.streamingWrapper(
                    request.getMessage(),
                    "deepseek-chat", // 使用 application.properties 中配置的 deepseek
                    Collections.emptyList(),
                    systemPrompt,
                    writer,
                    abortFlag
            );
            
            writer.flush();
        } catch (Exception e) {
            log.error("KB Chat streaming error: {}", e.getMessage());
        }
    }
}

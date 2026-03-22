package com.learnplatform.controller.api;

import com.learnplatform.dto.request.AiChatRequest;
import com.learnplatform.entity.Chapter;
import com.learnplatform.service.ChapterService;
import com.learnplatform.service.workflow.LlmStreamingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/api/ai/assistant")
@Tag(name = "AI助手", description = "AI 学习助手接口")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AiAssistantApiController {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantApiController.class);

    @Autowired
    private LlmStreamingService llmStreamingService;

    @Autowired
    private ChapterService chapterService;

    @Operation(summary = "章节提问（流式）", description = "针对特定章节内容向AI助手提问")
    @PostMapping("/chat")
    public void chat(
            @RequestBody AiChatRequest request,
            HttpServletResponse response
    ) {
        log.info("AI Chat request received for chapter: {}", request.getChapterId());

        Optional<Chapter> chapterOpt = chapterService.findChapterById(request.getChapterId());
        if (chapterOpt.isEmpty()) {
            response.setStatus(404);
            return;
        }

        Chapter chapter = chapterOpt.get();
        String chapterContent = chapter.getContent();
        if (chapterContent == null) chapterContent = "无章节内容";

        // 构建系统提示词，注入章节上下文
        String systemPrompt = String.format(
                "你是一个专业且耐心的课程学习助手。你正在帮助学生学习章节：\"%s\"。\n" +
                "本章内容如下：\n" +
                "---开始---\n" +
                "%s\n" +
                "---结束---\n\n" +
                "请严格基于以上内容回答学生的问题。如果问题超出了本章范围，请先简要回答，然后尝试将话题引导回本章相关的知识点。",
                chapter.getTitle(),
                chapterContent
        );

        // 设置响应头为 SSE
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        try (PrintWriter writer = response.getWriter()) {
            AtomicBoolean abortFlag = new AtomicBoolean(false);
            
            // 调用流式服务
            llmStreamingService.streamingWrapper(
                    request.getMessage(),
                    "deepseek-chat",
                    Collections.emptyList(), // 目前不支持多轮对话，只针对当前问题
                    systemPrompt,
                    writer,
                    abortFlag
            );
            
            writer.flush();
        } catch (Exception e) {
            log.error("AI Assistant streaming error: {}", e.getMessage());
        }
    }
}

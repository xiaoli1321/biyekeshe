package com.learnplatform.controller.api;

import com.learnplatform.dto.request.AiChatRequest;
import com.learnplatform.dto.workflow.HistoryMessage;
import com.learnplatform.entity.Chapter;
import com.learnplatform.entity.Concept;
import com.learnplatform.entity.Course;
import com.learnplatform.service.ChapterService;
import com.learnplatform.service.ConceptService;
import com.learnplatform.service.CourseService;
import com.learnplatform.service.GraphService;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private GraphService graphService;

    @Autowired
    private CourseService courseService;

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
        String courseName = resolveCourseName(chapter);
        List<String> chapterConcepts = conceptService.getConceptsByChapter(chapter.getId()).stream()
                .map(Concept::getName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .toList();
        List<HistoryMessage> history = trimHistory(request.getHistory(), 6);
        String prerequisiteText = conceptService.getConceptsByChapter(chapter.getId()).stream()
                .flatMap(concept -> graphService.getDirectPrerequisites(concept.getId()).stream())
                .map(Concept::getName)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), set ->
                        set.isEmpty() ? "无明确前置知识点" : String.join("、", set)
                ));
        String conceptText = chapterConcepts.isEmpty() ? "本章暂未配置显式知识点" : String.join("、", chapterConcepts);

        // 构建系统提示词，注入章节上下文和多轮问答约束
        String systemPrompt = String.format(
                "你是一个专业且耐心的课程学习助手，当前正在辅导《%s》中的章节“%s”。\n" +
                "请优先围绕当前章节进行讲解，回答要通俗、结构化，必要时先给结论再解释原因。\n" +
                "本章知识点：%s。\n" +
                "本章前置知识点：%s。\n" +
                "本章内容如下：\n" +
                "---开始---\n" +
                "%s\n" +
                "---结束---\n\n" +
                "请严格结合上述章节内容和最近几轮对话回答学生的问题。" +
                "如果问题超出了本章范围，请先简要说明，再主动关联回本章相关知识点。" +
                "如果学生提问含糊，请先结合当前章节语境补全问题再作答。",
                courseName,
                chapter.getTitle(),
                conceptText,
                prerequisiteText,
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
                    history,
                    systemPrompt,
                    writer,
                    abortFlag
            );
            
            writer.flush();
        } catch (Exception e) {
            log.error("AI Assistant streaming error: {}", e.getMessage());
        }
    }

    private String resolveCourseName(Chapter chapter) {
        String courseId = chapter.getCourseId();
        if (courseId == null || courseId.isBlank()) {
            return "当前课程";
        }
        return courseService.findCourseById(courseId)
                .map(Course::getName)
                .filter(name -> name != null && !name.isBlank())
                .orElse("当前课程");
    }

    private List<HistoryMessage> trimHistory(List<HistoryMessage> history, int maxSize) {
        if (history == null || history.isEmpty()) {
            return Collections.emptyList();
        }
        List<HistoryMessage> sanitized = history.stream()
                .filter(item -> item != null && item.getRole() != null && item.getContent() != null)
                .toList();
        if (sanitized.size() <= maxSize) {
            return sanitized;
        }
        return sanitized.subList(sanitized.size() - maxSize, sanitized.size());
    }
}

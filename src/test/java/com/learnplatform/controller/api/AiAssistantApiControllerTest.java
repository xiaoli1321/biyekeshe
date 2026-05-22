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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiAssistantApiControllerTest {

    @Mock
    private LlmStreamingService llmStreamingService;

    @Mock
    private ChapterService chapterService;

    @Mock
    private ConceptService conceptService;

    @Mock
    private GraphService graphService;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private AiAssistantApiController aiAssistantApiController;

    @Test
    void chatBuildsChapterAwarePromptAndPassesRecentHistory() {
        Course course = new Course();
        course.setId("course-1");
        course.setName("Java 入门");

        Chapter chapter = new Chapter();
        chapter.setId("chapter-1");
        chapter.setTitle("条件判断");
        chapter.setContent("if 和 switch 用于条件判断。");
        chapter.setCourse(course);

        Concept concept = new Concept();
        concept.setId("concept-1");
        concept.setName("条件分支");
        concept.setChapter(chapter);
        concept.setCourse(course);

        Concept prerequisite = new Concept();
        prerequisite.setId("concept-2");
        prerequisite.setName("变量与类型");

        AiChatRequest request = new AiChatRequest("chapter-1", "这一章的重点是什么？");
        request.setHistory(List.of(
                new HistoryMessage("user", "上一问"),
                new HistoryMessage("assistant", "上一答")
        ));

        when(chapterService.findChapterById("chapter-1")).thenReturn(Optional.of(chapter));
        when(courseService.findCourseById("course-1")).thenReturn(Optional.of(course));
        when(conceptService.getConceptsByChapter("chapter-1")).thenReturn(List.of(concept));
        when(graphService.getDirectPrerequisites("concept-1")).thenReturn(List.of(prerequisite));

        MockHttpServletResponse response = new MockHttpServletResponse();

        aiAssistantApiController.chat(request, response);

        ArgumentCaptor<List<HistoryMessage>> historyCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<String> systemPromptCaptor = ArgumentCaptor.forClass(String.class);

        verify(llmStreamingService).streamingWrapper(
                eq("这一章的重点是什么？"),
                eq("deepseek-chat"),
                historyCaptor.capture(),
                systemPromptCaptor.capture(),
                any(PrintWriter.class),
                any(AtomicBoolean.class)
        );

        assertEquals(2, historyCaptor.getValue().size());
        assertEquals("上一问", historyCaptor.getValue().get(0).getContent());
        assertTrue(systemPromptCaptor.getValue().contains("条件判断"));
        assertTrue(systemPromptCaptor.getValue().contains("Java 入门"));
        assertTrue(systemPromptCaptor.getValue().contains("变量与类型"));
    }
}

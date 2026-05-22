package com.learnplatform.controller.api;

import com.learnplatform.dto.ApiResponse;
import com.learnplatform.entity.Chapter;
import com.learnplatform.entity.Concept;
import com.learnplatform.entity.Course;
import com.learnplatform.service.ChapterService;
import com.learnplatform.service.ConceptService;
import com.learnplatform.service.CourseService;
import com.learnplatform.service.RelationshipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConceptApiControllerTest {

    @Mock
    private ConceptService conceptService;

    @Mock
    private RelationshipService relationshipService;

    @Mock
    private ChapterService chapterService;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private ConceptApiController conceptApiController;

    private Course course1;
    private Course course2;
    private Chapter chapterInCourse1;
    private Concept conceptInCourse1;
    private Concept conceptInCourse2;

    @BeforeEach
    void setUp() {
        course1 = new Course();
        course1.setId("course-1");
        course1.setName("课程1");

        course2 = new Course();
        course2.setId("course-2");
        course2.setName("课程2");

        chapterInCourse1 = new Chapter();
        chapterInCourse1.setId("chapter-1");
        chapterInCourse1.setCourse(course1);

        conceptInCourse1 = new Concept();
        conceptInCourse1.setId("concept-1");
        conceptInCourse1.setCourse(course1);
        conceptInCourse1.setChapter(chapterInCourse1);
        conceptInCourse1.setName("知识点1");

        Chapter chapterInCourse2 = new Chapter();
        chapterInCourse2.setId("chapter-2");
        chapterInCourse2.setCourse(course2);

        conceptInCourse2 = new Concept();
        conceptInCourse2.setId("concept-2");
        conceptInCourse2.setCourse(course2);
        conceptInCourse2.setChapter(chapterInCourse2);
        conceptInCourse2.setName("知识点2");
    }

    @Test
    void createConceptRejectsChapterAndCourseMismatch() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "新知识点");
        body.put("description", "desc");
        body.put("chapterId", "chapter-1");
        body.put("courseId", "course-2");

        when(chapterService.findChapterById("chapter-1")).thenReturn(Optional.of(chapterInCourse1));
        when(courseService.findCourseById("course-2")).thenReturn(Optional.of(course2));

        ApiResponse<Concept> response = conceptApiController.createConcept(body);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("章节不属于所选课程"));
    }

    @Test
    void createRelationshipRejectsCrossCourseRelationship() {
        Map<String, Object> body = new HashMap<>();
        body.put("fromConceptId", "concept-1");
        body.put("toConceptId", "concept-2");
        body.put("type", "PREREQUISITE");

        when(conceptService.findConceptById("concept-1")).thenReturn(Optional.of(conceptInCourse1));
        when(conceptService.findConceptById("concept-2")).thenReturn(Optional.of(conceptInCourse2));

        var response = conceptApiController.createRelationship(body);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("不能创建跨课程"));
    }

    @Test
    void createConceptPersistsRichContentFields() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "变量");
        body.put("description", "变量描述");
        body.put("summary", "用于存储数据的命名空间");
        body.put("content", "变量由类型、名称和值组成。");
        body.put("example", "int age = 18;");
        body.put("commonPitfall", "未初始化局部变量会导致编译错误");
        body.put("chapterId", "chapter-1");
        body.put("courseId", "course-1");
        body.put("difficultyLevel", 2);
        body.put("importanceWeight", 88);

        when(chapterService.findChapterById("chapter-1")).thenReturn(Optional.of(chapterInCourse1));
        when(courseService.findCourseById("course-1")).thenReturn(Optional.of(course1));
        when(conceptService.createConcept(any(Concept.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<Concept> response = conceptApiController.createConcept(body);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("用于存储数据的命名空间", response.getData().getSummary());
        assertEquals("变量由类型、名称和值组成。", response.getData().getContent());
        assertEquals("int age = 18;", response.getData().getExample());
        assertEquals("未初始化局部变量会导致编译错误", response.getData().getCommonPitfall());
    }
}

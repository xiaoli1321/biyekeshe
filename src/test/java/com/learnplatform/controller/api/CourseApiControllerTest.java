package com.learnplatform.controller.api;

import com.learnplatform.dto.ApiResponse;
import com.learnplatform.dto.response.CourseDto;
import com.learnplatform.entity.Chapter;
import com.learnplatform.entity.Course;
import com.learnplatform.service.ChapterService;
import com.learnplatform.service.CourseService;
import com.learnplatform.service.ProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseApiControllerTest {

    @Mock
    private CourseService courseService;

    @Mock
    private ChapterService chapterService;

    @Mock
    private ProgressService progressService;

    @InjectMocks
    private CourseApiController courseApiController;

    private Course course;
    private Chapter chapter;

    @BeforeEach
    void setUp() {
        course = new Course();
        course.setId("course-1");
        course.setName("Java");
        course.setChapters(List.of());

        chapter = new Chapter();
        chapter.setId("chapter-1");
        chapter.setTitle("第一章");
        chapter.setCourse(course);
        chapter.setOrderIndex(1);
    }

    @Test
    void getCourseByIdBuildsChapterListFromChapterRepositoryInsteadOfCourseDbRefList() {
        when(courseService.findCourseById("course-1")).thenReturn(Optional.of(course));
        when(chapterService.getChaptersByCourseOrdered("course-1")).thenReturn(List.of(chapter));

        ApiResponse<CourseDto> response = courseApiController.getCourseById("course-1");

        assertNotNull(response.getData());
        assertNotNull(response.getData().getChapters());
        assertEquals(1, response.getData().getChapters().size());
        assertEquals("chapter-1", response.getData().getChapters().get(0).getId());
    }
}

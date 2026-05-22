package com.learnplatform.service;

import com.learnplatform.repository.CourseRepository;
import com.learnplatform.repository.ProgressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ChapterService chapterService;

    @Mock
    private ConceptService conceptService;

    @Mock
    private ProgressRepository progressRepository;

    @Mock
    private ConceptProgressService conceptProgressService;

    @InjectMocks
    private CourseService courseService;

    @Test
    void deleteCourseCascadesCourseGraphAndProgressData() {
        courseService.deleteCourse("course-1");

        verify(chapterService).deleteChaptersByCourse("course-1");
        verify(conceptService).deleteConceptsByCourse("course-1");
        verify(progressRepository).deleteByCourse_Id("course-1");
        verify(conceptProgressService).deleteByCourseId("course-1");
        verify(courseRepository).deleteById("course-1");
    }
}

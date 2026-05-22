package com.learnplatform.service;

import com.learnplatform.entity.Chapter;
import com.learnplatform.entity.Concept;
import com.learnplatform.entity.ConceptProgress;
import com.learnplatform.entity.Course;
import com.learnplatform.entity.Progress;
import com.learnplatform.entity.Relationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphServiceTest {

    @Mock
    private ConceptService conceptService;

    @Mock
    private RelationshipService relationshipService;

    @Mock
    private ProgressService progressService;

    @Mock
    private ConceptProgressService conceptProgressService;

    @InjectMocks
    private GraphService graphService;

    private Concept dataTypes;
    private Concept variables;
    private Concept classes;
    private Relationship dataTypesToVariables;
    private Relationship variablesToClasses;

    @BeforeEach
    void setUp() {
        Course course = new Course();
        course.setId("course-1");
        course.setName("Java");

        Chapter chapter1 = new Chapter();
        chapter1.setId("chapter-1");
        chapter1.setTitle("基础语法");
        chapter1.setCourse(course);

        Chapter chapter2 = new Chapter();
        chapter2.setId("chapter-2");
        chapter2.setTitle("面向对象");
        chapter2.setCourse(course);

        dataTypes = buildConcept("concept-data-types", "数据类型", 1, 90, course, chapter1);
        variables = buildConcept("concept-variables", "变量", 1, 85, course, chapter1);
        classes = buildConcept("concept-classes", "类和对象", 3, 95, course, chapter2);

        dataTypesToVariables = new Relationship(
                dataTypes,
                variables,
                Relationship.RelationshipType.PREREQUISITE,
                0.8,
                "变量依赖于数据类型"
        );
        dataTypesToVariables.setId("rel-1");

        variablesToClasses = new Relationship(
                variables,
                classes,
                Relationship.RelationshipType.PREREQUISITE,
                0.7,
                "类和对象使用变量"
        );
        variablesToClasses.setId("rel-2");
    }

    @Test
    void getDirectPrerequisitesReturnsIncomingPrerequisiteConcepts() {
        when(relationshipService.getIncomingRelationships(classes.getId()))
                .thenReturn(List.of(variablesToClasses));

        List<Concept> prerequisites = graphService.getDirectPrerequisites(classes.getId());

        assertEquals(1, prerequisites.size());
        assertEquals(variables.getId(), prerequisites.get(0).getId());
    }

    @Test
    void generateLearningPathOrdersConceptsByPrerequisiteDirectionAndSkipsCompletedChapterConcepts() {
        when(conceptService.getConceptsByCourse("course-1"))
                .thenReturn(List.of(classes, variables, dataTypes));
        when(relationshipService.getRelationshipsByCourse("course-1"))
                .thenReturn(List.of(variablesToClasses, dataTypesToVariables));

        Progress completedChapterProgress = new Progress();
        completedChapterProgress.setChapter(variables.getChapter());
        completedChapterProgress.setStatus(Progress.ProgressStatus.COMPLETED);

        when(progressService.getUserCourseProgress("user-1", "course-1"))
                .thenReturn(List.of(completedChapterProgress));

        List<Concept> path = graphService.generateLearningPath("user-1", "course-1");

        assertIterableEquals(
                List.of(classes.getId()),
                path.stream().map(Concept::getId).toList()
        );
    }

    @Test
    void canStudyConceptReturnsTrueWhenPrerequisitesAlreadyCompleted() {
        Progress completedChapterProgress = new Progress();
        completedChapterProgress.setChapter(dataTypes.getChapter());
        completedChapterProgress.setStatus(Progress.ProgressStatus.COMPLETED);

        when(relationshipService.getIncomingRelationships(variables.getId()))
                .thenReturn(List.of(dataTypesToVariables));
        when(progressService.getUserCourseProgress("user-1", "course-1"))
                .thenReturn(List.of(completedChapterProgress));

        assertTrue(graphService.canStudyConcept("user-1", "course-1", variables.getId()));
    }

    @Test
    void canStudyConceptReturnsFalseWhenAnyPrerequisiteChapterIsIncomplete() {
        when(relationshipService.getIncomingRelationships(variables.getId()))
                .thenReturn(List.of(dataTypesToVariables));
        when(progressService.getUserCourseProgress("user-1", "course-1"))
                .thenReturn(List.of());

        assertFalse(graphService.canStudyConcept("user-1", "course-1", variables.getId()));
    }

    @Test
    void getFullGraphDataPrefersConceptProgressOverChapterFallback() {
        when(conceptService.getConceptsByCourse("course-1"))
                .thenReturn(List.of(dataTypes, variables));
        when(relationshipService.getRelationshipsByCourse("course-1"))
                .thenReturn(List.of(dataTypesToVariables));

        Progress completedChapterProgress = new Progress();
        completedChapterProgress.setChapter(dataTypes.getChapter());
        completedChapterProgress.setStatus(Progress.ProgressStatus.COMPLETED);

        ConceptProgress variablesProgress = new ConceptProgress();
        variablesProgress.setConcept(variables);
        variablesProgress.setStatus(ConceptProgress.ProgressStatus.MASTERED);

        when(progressService.getUserCourseProgress("user-1", "course-1"))
                .thenReturn(List.of(completedChapterProgress));
        when(conceptProgressService.getUserCourseConceptProgress("user-1", "course-1"))
                .thenReturn(List.of(variablesProgress));

        var graphData = graphService.getFullGraphData("course-1", "user-1");

        assertEquals(
                "MASTERED",
                graphData.getNodes().stream()
                        .filter(node -> variables.getId().equals(node.getId()))
                        .findFirst()
                        .orElseThrow()
                        .getProgressStatus()
        );
        assertEquals(
                "COMPLETED",
                graphData.getNodes().stream()
                        .filter(node -> dataTypes.getId().equals(node.getId()))
                        .findFirst()
                        .orElseThrow()
                        .getProgressStatus()
        );
    }

    @Test
    void generateLearningPathSkipsOnlyCompletedConceptsInsteadOfWholeCompletedChapter() {
        when(conceptService.getConceptsByCourse("course-1"))
                .thenReturn(List.of(classes, variables, dataTypes));
        when(relationshipService.getRelationshipsByCourse("course-1"))
                .thenReturn(List.of(variablesToClasses, dataTypesToVariables));

        Progress completedChapterProgress = new Progress();
        completedChapterProgress.setChapter(variables.getChapter());
        completedChapterProgress.setStatus(Progress.ProgressStatus.COMPLETED);

        ConceptProgress variablesProgress = new ConceptProgress();
        variablesProgress.setConcept(variables);
        variablesProgress.setStatus(ConceptProgress.ProgressStatus.COMPLETED);

        when(progressService.getUserCourseProgress("user-1", "course-1"))
                .thenReturn(List.of(completedChapterProgress));
        when(conceptProgressService.getUserCourseConceptProgress("user-1", "course-1"))
                .thenReturn(List.of(variablesProgress));

        List<Concept> path = graphService.generateLearningPath("user-1", "course-1");

        assertIterableEquals(
                List.of(dataTypes.getId(), classes.getId()),
                path.stream().map(Concept::getId).toList()
        );
    }

    private Concept buildConcept(
            String id,
            String name,
            int difficultyLevel,
            int importanceWeight,
            Course course,
            Chapter chapter
    ) {
        Concept concept = new Concept();
        concept.setId(id);
        concept.setName(name);
        concept.setSummary(name + "摘要");
        concept.setContent(name + "详细讲解");
        concept.setExample(name + "示例");
        concept.setCommonPitfall(name + "常见误区");
        concept.setDifficultyLevel(difficultyLevel);
        concept.setImportanceWeight(importanceWeight);
        concept.setCourse(course);
        concept.setChapter(chapter);
        return concept;
    }

    @Test
    void getFullGraphDataIncludesRichConceptContentAndChapterGrouping() {
        when(conceptService.getConceptsByCourse("course-1"))
                .thenReturn(List.of(dataTypes, variables, classes));
        when(relationshipService.getRelationshipsByCourse("course-1"))
                .thenReturn(List.of(dataTypesToVariables, variablesToClasses));
        when(progressService.getUserCourseProgress("user-1", "course-1"))
                .thenReturn(List.of());
        when(conceptProgressService.getUserCourseConceptProgress("user-1", "course-1"))
                .thenReturn(List.of());

        var graphData = graphService.getFullGraphData("course-1", "user-1");
        var variableNode = graphData.getNodes().stream()
                .filter(node -> variables.getId().equals(node.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals("变量摘要", variableNode.getSummary());
        assertEquals("变量详细讲解", variableNode.getContent());
        assertEquals("变量示例", variableNode.getExample());
        assertEquals("变量常见误区", variableNode.getCommonPitfall());
        assertEquals("chapter-1", variableNode.getChapterGroupId());
        assertEquals(2, graphData.getStatistics().getChapterGroups().get("基础语法"));
        assertEquals(1, graphData.getStatistics().getChapterGroups().get("面向对象"));
    }
}

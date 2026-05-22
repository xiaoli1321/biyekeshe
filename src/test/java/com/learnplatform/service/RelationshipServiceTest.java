package com.learnplatform.service;

import com.learnplatform.entity.Chapter;
import com.learnplatform.entity.Concept;
import com.learnplatform.entity.Course;
import com.learnplatform.entity.Relationship;
import com.learnplatform.repository.ConceptRepository;
import com.learnplatform.repository.RelationshipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelationshipServiceTest {

    @Mock
    private RelationshipRepository relationshipRepository;

    @Mock
    private ConceptRepository conceptRepository;

    @InjectMocks
    private RelationshipService relationshipService;

    private Concept course1ConceptA;
    private Concept course1ConceptB;
    private Concept course2Concept;
    private Relationship inCourseRelationship;
    private Relationship crossCourseRelationship;

    @BeforeEach
    void setUp() {
        Course course1 = new Course();
        course1.setId("course-1");

        Course course2 = new Course();
        course2.setId("course-2");

        Chapter chapter1 = new Chapter();
        chapter1.setId("chapter-1");
        chapter1.setCourse(course1);

        Chapter chapter2 = new Chapter();
        chapter2.setId("chapter-2");
        chapter2.setCourse(course2);

        course1ConceptA = buildConcept("concept-a", "A", course1, chapter1);
        course1ConceptB = buildConcept("concept-b", "B", course1, chapter1);
        course2Concept = buildConcept("concept-c", "C", course2, chapter2);

        inCourseRelationship = new Relationship(course1ConceptA, course1ConceptB, Relationship.RelationshipType.PREREQUISITE, 1.0, "in-course");
        inCourseRelationship.setId("rel-1");

        crossCourseRelationship = new Relationship(course1ConceptA, course2Concept, Relationship.RelationshipType.PREREQUISITE, 1.0, "cross-course");
        crossCourseRelationship.setId("rel-2");
    }

    @Test
    void getRelationshipsByCourseReturnsOnlyRelationshipsWhoseBothEndpointsBelongToTheCourse() {
        when(conceptRepository.findByCourse_Id("course-1"))
                .thenReturn(List.of(course1ConceptA, course1ConceptB));
        when(relationshipRepository.findAll())
                .thenReturn(List.of(inCourseRelationship, crossCourseRelationship));

        List<Relationship> relationships = relationshipService.getRelationshipsByCourse("course-1");

        assertEquals(1, relationships.size());
        assertIterableEquals(List.of("rel-1"), relationships.stream().map(Relationship::getId).toList());
    }

    private Concept buildConcept(String id, String name, Course course, Chapter chapter) {
        Concept concept = new Concept();
        concept.setId(id);
        concept.setName(name);
        concept.setCourse(course);
        concept.setChapter(chapter);
        return concept;
    }
}

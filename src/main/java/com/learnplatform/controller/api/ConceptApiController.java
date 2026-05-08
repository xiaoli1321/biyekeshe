package com.learnplatform.controller.api;

import com.learnplatform.dto.ApiResponse;
import com.learnplatform.entity.Chapter;
import com.learnplatform.entity.Concept;
import com.learnplatform.entity.Course;
import com.learnplatform.entity.Relationship;
import com.learnplatform.service.ChapterService;
import com.learnplatform.service.ConceptService;
import com.learnplatform.service.CourseService;
import com.learnplatform.service.RelationshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "知识点管理", description = "课程知识点与关系的CRUD接口")
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ConceptApiController {

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private RelationshipService relationshipService;

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private CourseService courseService;

    @Operation(summary = "获取章节的知识点列表")
    @GetMapping("/chapters/{chapterId}/concepts")
    public ApiResponse<List<Concept>> getChapterConcepts(@PathVariable String chapterId) {
        List<Concept> concepts = conceptService.getConceptsByChapter(chapterId);
        return ApiResponse.success(concepts, "获取知识点成功");
    }

    @Operation(summary = "创建知识点")
    @PostMapping("/concepts")
    public ApiResponse<Concept> createConcept(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String description = (String) body.get("description");
        String chapterId = (String) body.get("chapterId");
        String courseId = (String) body.get("courseId");
        Integer difficultyLevel = body.get("difficultyLevel") != null ? ((Number) body.get("difficultyLevel")).intValue() : 1;
        Integer importanceWeight = body.get("importanceWeight") != null ? ((Number) body.get("importanceWeight")).intValue() : 50;

        Chapter chapter = chapterService.findChapterById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("章节未找到"));
        Course course = courseService.findCourseById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程未找到"));

        Concept concept = new Concept(course, chapter, name, description);
        concept.setDifficultyLevel(difficultyLevel);
        concept.setImportanceWeight(importanceWeight);
        Concept saved = conceptService.createConcept(concept);
        return ApiResponse.success(saved, "知识点创建成功");
    }

    @Operation(summary = "更新知识点")
    @PutMapping("/concepts/{id}")
    public ApiResponse<Concept> updateConcept(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Concept concept = conceptService.findConceptById(id)
                .orElseThrow(() -> new IllegalArgumentException("知识点未找到"));
        if (body.containsKey("name")) concept.setName((String) body.get("name"));
        if (body.containsKey("description")) concept.setDescription((String) body.get("description"));
        if (body.containsKey("difficultyLevel")) concept.setDifficultyLevel(((Number) body.get("difficultyLevel")).intValue());
        if (body.containsKey("importanceWeight")) concept.setImportanceWeight(((Number) body.get("importanceWeight")).intValue());
        Concept saved = conceptService.updateConcept(concept);
        return ApiResponse.success(saved, "知识点更新成功");
    }

    @Operation(summary = "删除知识点")
    @DeleteMapping("/concepts/{id}")
    public ApiResponse<Void> deleteConcept(@PathVariable String id) {
        conceptService.deleteConcept(id);
        return ApiResponse.success(null, "知识点删除成功");
    }

    @Operation(summary = "创建知识点关系")
    @PostMapping("/relationships")
    public ApiResponse<Relationship> createRelationship(@RequestBody Map<String, Object> body) {
        String fromConceptId = (String) body.get("fromConceptId");
        String toConceptId = (String) body.get("toConceptId");
        String type = (String) body.get("type");
        Double weight = body.get("weight") != null ? ((Number) body.get("weight")).doubleValue() : 1.0;
        String description = (String) body.get("description");

        Concept from = conceptService.findConceptById(fromConceptId)
                .orElseThrow(() -> new IllegalArgumentException("源知识点未找到"));
        Concept to = conceptService.findConceptById(toConceptId)
                .orElseThrow(() -> new IllegalArgumentException("目标知识点未找到"));

        Relationship.RelationshipType relType = Relationship.RelationshipType.valueOf(type);
        Relationship rel = new Relationship(from, to, relType, weight, description);
        Relationship saved = relationshipService.createRelationship(rel);
        return ApiResponse.success(saved, "关系创建成功");
    }

    @Operation(summary = "删除知识点关系")
    @DeleteMapping("/relationships/{id}")
    public ApiResponse<Void> deleteRelationship(@PathVariable String id) {
        relationshipService.deleteRelationship(id);
        return ApiResponse.success(null, "关系删除成功");
    }

    @Operation(summary = "获取课程所有知识点")
    @GetMapping("/courses/{courseId}/concepts")
    public ApiResponse<List<Concept>> getCourseConcepts(@PathVariable String courseId) {
        List<Concept> concepts = conceptService.getConceptsByCourse(courseId);
        return ApiResponse.success(concepts, "获取课程知识点成功");
    }

    @Operation(summary = "获取课程所有关系")
    @GetMapping("/courses/{courseId}/relationships")
    public ApiResponse<List<Relationship>> getCourseRelationships(@PathVariable String courseId) {
        List<Relationship> rels = relationshipService.getRelationshipsByCourse(courseId);
        return ApiResponse.success(rels, "获取课程关系成功");
    }
}

package com.learnplatform.service;

import com.learnplatform.entity.Concept;
import com.learnplatform.repository.ConceptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 知识点业务服务
 */
@Service
@Transactional
public class ConceptService {

    @Autowired
    private ConceptRepository conceptRepository;

    @Autowired
    private RelationshipService relationshipService;

    @Autowired
    private ConceptProgressService conceptProgressService;

    /**
     * 创建知识点
     */
    public Concept createConcept(Concept concept) {
        return conceptRepository.save(concept);
    }

    /**
     * 更新知识点
     */
    public Concept updateConcept(Concept concept) {
        return conceptRepository.save(concept);
    }

    /**
     * 根据ID查找知识点
     */
    public Optional<Concept> findConceptById(String id) {
        return conceptRepository.findById(id);
    }

    /**
     * 根据课程查找知识点
     */
    public List<Concept> getConceptsByCourse(String courseId) {
        return conceptRepository.findByCourse_Id(courseId);
    }

    /**
     * 根据章节查找知识点
     */
    public List<Concept> getConceptsByChapter(String chapterId) {
        return conceptRepository.findByChapter_Id(chapterId);
    }

    /**
     * 根据课程查找核心理念点
     */
    public List<Concept> getCoreConcepts(String courseId, Integer minWeight) {
        Integer threshold = minWeight != null ? minWeight : 80;
        return conceptRepository.findByCourse_IdAndImportanceWeightGreaterThanEqual(courseId, threshold);
    }

    /**
     * 根据课程查找初级知识点
     */
    public List<Concept> getBeginnerConcepts(String courseId) {
        return conceptRepository.findByCourse_IdAndDifficultyLevelLessThanEqual(courseId, 2);
    }

    /**
     * 根据课程查找高级知识点
     */
    public List<Concept> getAdvancedConcepts(String courseId) {
        return conceptRepository.findByCourse_IdAndDifficultyLevelGreaterThanEqual(courseId, 3);
    }

    /**
     * 删除知识点
     */
    public void deleteConcept(String id) {
        conceptProgressService.deleteByConceptId(id);
        relationshipService.deleteRelationshipsByConceptId(id);
        conceptRepository.deleteById(id);
    }

    public void deleteConceptsByChapter(String chapterId) {
        getConceptsByChapter(chapterId).stream()
                .map(Concept::getId)
                .toList()
                .forEach(this::deleteConcept);
    }

    public void deleteConceptsByCourse(String courseId) {
        getConceptsByCourse(courseId).stream()
                .map(Concept::getId)
                .toList()
                .forEach(this::deleteConcept);
    }

    /**
     * 统计课程的知识点数量
     */
    public long countConceptsByCourse(String courseId) {
        return conceptRepository.countByCourse_Id(courseId);
    }

    /**
     * 按难度统订知识点数量
     */
    public List<Object[]> getConceptStatistics(String courseId) {
        // MongoDB repository doesn't strictly return List<Object[]> for simple counts usually unless aggregation
        // For now let's assume this might need adjustment, but fixing ID type first.
        // Also referencing countByDifficultyLevel which was missing in compilation errors
        // Actually the compilation error said countByDifficultyLevel(Long) not found.
        // ConceptRepository has: List<Concept> findByDifficultyLevel(Integer difficultyLevel);
        // It does NOT have countByDifficultyLevel that returns List<Object[]> or takes a CourseId.
        // I will assume for now we want to count by difficulty for a course, which would need an aggregation or simple logic.
        // Let's implement a workaround using existing methods if possible or assume repository is fixed.
        // Wait, ConceptRepository outline showed methods like findByCourseIdAndDifficultyLevel.
        // I will fix the ID type here. The logic might still be broken if the repo method doesn't exist.
        // The error log said: method countByDifficultyLevel(java.lang.Long) location: variable conceptRepository of type ... ConceptRepository
        // I should stick to what ConceptRepository has or just fix the type.
        // Let's look at ConceptRepository again?
        // It has `List<Concept> findByCourseId(String courseId);`
        // I'll leave the method call as is but fix the argument type, acknowledging it might still fail if method missing.
        // Wait, looking at ConceptRepository earlier content... it does NOT have countByDifficultyLevel(String courseID).
        // It has `List<Concept> findByDifficultyLevel(Integer difficultyLevel);`
        // I should probably remove or comment out this method if the repo support is missing, or better, implement it using multiple queries or just return empty for now to fix compilation.
        // Actually, let's just fix the ID type and let the compiler tell us if the method is still missing (it will be).
        // Better: I will replace the call with something valid or comment it out.
        // Since I can't easily add complex aggregation to interface without `@Aggregation` annotation in repo file (which I didn't verify if I can edit freely without breaking other things),
        // I'll change implementation to do in-memory counting for now to ensure compilation passes.
        List<Concept> concepts = conceptRepository.findByCourse_Id(courseId);
        // Group by difficulty
         java.util.Map<Integer, Long> counts = concepts.stream()
            .collect(java.util.stream.Collectors.groupingBy(Concept::getDifficultyLevel, java.util.stream.Collectors.counting()));
         
         List<Object[]> result = new java.util.ArrayList<>();
         counts.forEach((k, v) -> result.add(new Object[]{k, v}));
         return result;
    }
}

package com.learnplatform.service;

import com.learnplatform.entity.Concept;
import com.learnplatform.entity.Relationship;
import com.learnplatform.repository.ConceptRepository;
import com.learnplatform.repository.RelationshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;

/**
 * 关系业务服务
 */
@Service
@Transactional
public class RelationshipService {

    @Autowired
    private RelationshipRepository relationshipRepository;

    @Autowired
    private ConceptRepository conceptRepository;

    /**
     * 创建关系
     */
    public Relationship createRelationship(Relationship relationship) {
        // 检查是否已存在相同关系
        Optional<Relationship> existing = relationshipRepository
                .findByFromConcept_IdAndToConcept_IdAndRelationshipType(
                        relationship.getFromConcept().getId(),
                        relationship.getToConcept().getId(),
                        relationship.getRelationshipType()
                );

        if (existing.isPresent()) {
            throw new IllegalArgumentException("该关系已存在");
        }

        return relationshipRepository.save(relationship);
    }

    /**
     * 更新关系
     */
    public Relationship updateRelationship(Relationship relationship) {
        return relationshipRepository.save(relationship);
    }

    /**
     * 根据ID查找关系
     */
    public Optional<Relationship> findRelationshipById(String id) {
        return relationshipRepository.findById(id);
    }

    /**
     * 查找源节点的所有关系
     */
    public List<Relationship> getOutgoingRelationships(String conceptId) {
        return relationshipRepository.findByFromConcept_Id(conceptId);
    }

    /**
     * 查找目标节点的所有关系
     */
    public List<Relationship> getIncomingRelationships(String conceptId) {
        return relationshipRepository.findByToConcept_Id(conceptId);
    }

    /**
     * 查找两个概念之间的所有关系
     */
    public List<Relationship> getRelationshipsBetweenConcepts(String fromConceptId, String toConceptId) {
        return relationshipRepository.findByFromConcept_IdAndToConcept_Id(fromConceptId, toConceptId);
    }

    /**
     * 根据关系类型查找关系
     */
    public List<Relationship> getRelationshipsByType(Relationship.RelationshipType type) {
        return relationshipRepository.findByRelationshipType(type);
    }

    /**
     * 删除关系
     */
    public void deleteRelationship(String id) {
        relationshipRepository.deleteById(id);
    }

    /**
     * 删除两个概念之间的特定关系
     */
    public void deleteRelationship(String fromConceptId, String toConceptId, Relationship.RelationshipType type) {
        Optional<Relationship> relationship = relationshipRepository
                .findByFromConcept_IdAndToConcept_IdAndRelationshipType(fromConceptId, toConceptId, type);

        relationship.ifPresent(r -> relationshipRepository.deleteById(r.getId()));
    }

    /**
     * 查找课程的所有关系（逐个查询确保 DBRef 兼容性）
     */
    public List<Relationship> getRelationshipsByCourse(String courseId) {
        List<Concept> concepts = conceptRepository.findByCourse_Id(courseId);
        if (concepts == null || concepts.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        Set<Relationship> allRels = new LinkedHashSet<>();
        for (Concept c : concepts) {
            allRels.addAll(relationshipRepository.findByFromConcept_Id(c.getId()));
            allRels.addAll(relationshipRepository.findByToConcept_Id(c.getId()));
        }
        return new ArrayList<>(allRels);
    }

    /**
     * 按关系类型统订数量
     */
    public List<Object[]> getRelationshipStatistics() {
        // Fix for countByRelationshipType() which might be missing in repo
        // Using in-memory calculation 
        List<Relationship> all = relationshipRepository.findAll();
        java.util.Map<Relationship.RelationshipType, Long> counts = all.stream()
            .collect(java.util.stream.Collectors.groupingBy(Relationship::getRelationshipType, java.util.stream.Collectors.counting()));
        
        List<Object[]> result = new java.util.ArrayList<>();
        counts.forEach((k, v) -> result.add(new Object[]{k, v}));
        return result;
    }
}

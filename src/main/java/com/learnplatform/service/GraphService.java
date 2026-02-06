package com.learnplatform.service;

import com.learnplatform.dto.GraphData;
import com.learnplatform.entity.Concept;
import com.learnplatform.entity.Relationship;
import com.learnplatform.entity.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识图谱业务服务
 */
@Service
@Transactional
public class GraphService {

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private RelationshipService relationshipService;

    /**
     * 获取课程的知识图谱数据
     */
    public GraphData getCourseGraphData(String courseId) {
        List<Concept> concepts = conceptService.getConceptsByCourse(courseId);
        List<Relationship> relationships = relationshipService.getRelationshipsByCourse(courseId);

        // 转换为前端需要的格式
        List<GraphData.Node> nodes = concepts.stream()
                .map(this::convertToNode)
                .collect(Collectors.toList());

        List<GraphData.Link> links = relationships.stream()
                .map(this::convertToLink)
                .collect(Collectors.toList());

        return new GraphData(nodes, links);
    }

    /**
     * 获取概念的所有先修知识点（递归）
     */
    public List<Concept> getAllPrerequisites(String conceptId) {
        Set<Concept> prerequisites = new HashSet<>();
        findPrerequisitesRecursive(conceptId, prerequisites);
        return new ArrayList<>(prerequisites);
    }

    /**
     * 递归查找先修知识点
     */
    private void findPrerequisitesRecursive(String conceptId, Set<Concept> prerequisites) {
        List<Relationship> incomingRels = relationshipService.getIncomingRelationships(conceptId);

        for (Relationship rel : incomingRels) {
            if (rel.getRelationshipType() == Relationship.RelationshipType.PREREQUISITE) {
                Concept prerequisite = rel.getFromConcept();
                if (!prerequisites.contains(prerequisite)) {
                    prerequisites.add(prerequisite);
                    findPrerequisitesRecursive(prerequisite.getId(), prerequisites);
                }
            }
        }
    }

    /**
     * 获取概念的所有依赖概念（递归）
     */
    public List<Concept> getAllDependents(String conceptId) {
        Set<Concept> dependents = new HashSet<>();
        findDependentsRecursive(conceptId, dependents);
        return new ArrayList<>(dependents);
    }

    /**
     * 递归查找依赖概念
     */
    private void findDependentsRecursive(String conceptId, Set<Concept> dependents) {
        List<Relationship> outgoingRels = relationshipService.getOutgoingRelationships(conceptId);

        for (Relationship rel : outgoingRels) {
            if (rel.getRelationshipType() == Relationship.RelationshipType.PREREQUISITE) {
                Concept dependent = rel.getToConcept();
                if (!dependents.contains(dependent)) {
                    dependents.add(dependent);
                    findDependentsRecursive(dependent.getId(), dependents);
                }
            }
        }
    }

    /**
     * 生成个性化学习路径
     */
    public List<Concept> generateLearningPath(String userId, String courseId) {
        // 获取课程的所有概念
        List<Concept> allConcepts = conceptService.getConceptsByCourse(courseId);

        // 按难度排序
        allConcepts.sort(Comparator.comparing(Concept::getDifficultyLevel));

        // 构建学习路径（在实际应用中，这里应该考虑用户的实际进度）
        List<Concept> learningPath = new ArrayList<>();
        for (Concept concept : allConcepts) {
            List<Concept> prerequisites = getAllPrerequisites(concept.getId());
            boolean prerequisitesMet = prerequisites.isEmpty() || prerequisites.stream()
                    .allMatch(p -> isPrerequisitesCompleted(p.getId(), userId));

            if (prerequisitesMet) {
                learningPath.add(concept);
            }
        }

        return learningPath;
    }

    /**
     * 检查先修知识点是否完成（在实际应用中需要从ProgressService获取）
     */
    private boolean isPrerequisitesCompleted(String conceptId, String userId) {
        // 简化实现：返回true或false
        // 在实际应用中，需要检查用户的学习进度
        return true;
    }

    /**
     * 获取概念的核心理念点（高重要程度）
     */
    public List<Concept> getKeyConcepts(String courseId) {
        return conceptService.getCoreConcepts(courseId, 80);
    }

    /**
     * 获取概念的先修知识点
     */
    public List<Concept> getDirectPrerequisites(String conceptId) {
        List<Relationship> incomingRels = relationshipService.getIncomingRelationships(conceptId);
        return incomingRels.stream()
                .filter(rel -> rel.getRelationshipType() == Relationship.RelationshipType.PREREQUISITE)
                .map(Relationship::getFromConcept)
                .collect(Collectors.toList());
    }

    /**
     * 获取相关概念（按名称匹配）
     */
    public List<String> getRelatedConcepts(String conceptName, String courseId) {
        if (conceptName == null || conceptName.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }

        String keyword = conceptName.trim().toLowerCase();
        List<Concept> concepts = conceptService.getConceptsByCourse(courseId);

        return concepts.stream()
                .map(Concept::getName)
                .filter(name -> name != null && !name.equals(conceptName))
                .filter(name -> name.toLowerCase().contains(keyword))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 检查概念是否可以学习（所有先修知识点都已掌握）
     */
    public boolean canStudyConcept(String conceptId) {
        List<Relationship> incomingRels = relationshipService.getIncomingRelationships(conceptId);
        for (Relationship rel : incomingRels) {
            if (rel.getRelationshipType() == Relationship.RelationshipType.PREREQUISITE) {
                // 在实际应用中需要检查先修知识点是否已掌握
                return false;
            }
        }
        return true;
    }

    /**
     * 转换为节点格式
     */
    private GraphData.Node convertToNode(Concept concept) {
        return new GraphData.Node(
                concept.getId().toString(),
                concept.getName(),
                concept.getDifficultyLevel(),
                concept.getImportanceWeight()
        );
    }

    /**
     * 转换为连接格式
     */
    private GraphData.Link convertToLink(Relationship relationship) {
        return new GraphData.Link(
                relationship.getFromConcept().getId().toString(),
                relationship.getToConcept().getId().toString(),
                relationship.getRelationshipType().name(),
                relationship.getWeight()
        );
    }
}

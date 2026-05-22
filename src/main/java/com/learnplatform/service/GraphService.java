package com.learnplatform.service;

import com.learnplatform.dto.GraphData;
import com.learnplatform.entity.Concept;
import com.learnplatform.entity.ConceptProgress;
import com.learnplatform.entity.Progress;
import com.learnplatform.entity.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识图谱业务服务（增强版：图算法 + 进度融合）
 */
@Service
@Transactional
public class GraphService {

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private RelationshipService relationshipService;

    @Autowired
    private ProgressService progressService;

    @Autowired
    private ConceptProgressService conceptProgressService;

    // ==================== 核心图谱数据 ====================

    /**
     * 获取课程完整知识图谱（含用户进度）
     */
    public GraphData getFullGraphData(String courseId, String userId) {
        List<Concept> concepts = conceptService.getConceptsByCourse(courseId);
        List<Relationship> relationships = relationshipService.getRelationshipsByCourse(courseId);
        List<Progress> userProgress = progressService.getUserCourseProgress(userId, courseId);
        List<ConceptProgress> conceptProgressList = conceptProgressService.getUserCourseConceptProgress(userId, courseId);

        Map<String, Progress.ProgressStatus> chapterStatusMap = buildChapterStatusMap(userProgress);
        Map<String, ConceptProgress.ProgressStatus> conceptStatusMap = buildConceptStatusMap(conceptProgressList);

        List<GraphData.GraphNode> nodes = concepts.stream()
                .map(c -> convertToGraphNode(c, chapterStatusMap, conceptStatusMap))
                .collect(Collectors.toList());

        List<GraphData.GraphLink> links = relationships.stream()
                .map(this::convertToGraphLink)
                .collect(Collectors.toList());

        GraphData.GraphStatistics stats = buildStatistics(concepts, relationships, nodes);

        return new GraphData(nodes, links, stats);
    }

    /**
     * 获取课程知识图谱数据（无用户进度）
     */
    public GraphData getCourseGraphData(String courseId) {
        List<Concept> concepts = conceptService.getConceptsByCourse(courseId);
        List<Relationship> relationships = relationshipService.getRelationshipsByCourse(courseId);

        List<GraphData.GraphNode> nodes = concepts.stream()
                .map(c -> convertToGraphNode(c, Collections.emptyMap(), Collections.emptyMap()))
                .collect(Collectors.toList());

        List<GraphData.GraphLink> links = relationships.stream()
                .map(this::convertToGraphLink)
                .collect(Collectors.toList());

        return new GraphData(nodes, links);
    }

    // ==================== 图算法 ====================

    /**
     * BFS 最短路径（返回路径上的概念ID列表）
     */
    public List<String> findShortestPath(String fromId, String toId) {
        if (fromId.equals(toId)) {
            return List.of(fromId);
        }

        Queue<String> queue = new LinkedList<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.add(fromId);
        visited.add(fromId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            List<Relationship> outgoing = relationshipService.getOutgoingRelationships(current);

            for (Relationship rel : outgoing) {
                String neighborId;
                if (rel.getFromConcept().getId().equals(current)) {
                    neighborId = rel.getToConcept().getId();
                } else {
                    neighborId = rel.getFromConcept().getId();
                }

                if (!visited.contains(neighborId)) {
                    visited.add(neighborId);
                    parent.put(neighborId, current);
                    queue.add(neighborId);

                    if (neighborId.equals(toId)) {
                        return reconstructPath(parent, fromId, toId);
                    }
                }
            }

            // also check incoming relationships
            List<Relationship> incoming = relationshipService.getIncomingRelationships(current);
            for (Relationship rel : incoming) {
                String neighborId;
                if (rel.getToConcept().getId().equals(current)) {
                    neighborId = rel.getFromConcept().getId();
                } else {
                    neighborId = rel.getToConcept().getId();
                }

                if (!visited.contains(neighborId)) {
                    visited.add(neighborId);
                    parent.put(neighborId, current);
                    queue.add(neighborId);

                    if (neighborId.equals(toId)) {
                        return reconstructPath(parent, fromId, toId);
                    }
                }
            }
        }

        return Collections.emptyList(); // 无路径
    }

    private List<String> reconstructPath(Map<String, String> parent, String fromId, String toId) {
        List<String> path = new ArrayList<>();
        String current = toId;
        while (current != null) {
            path.add(current);
            current = parent.get(current);
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * 简版 PageRank 中心度计算
     */
    public Map<String, Double> calculateCentrality(String courseId) {
        List<Concept> concepts = conceptService.getConceptsByCourse(courseId);
        List<Relationship> relationships = relationshipService.getRelationshipsByCourse(courseId);

        Map<String, Double> scores = new HashMap<>();
        int n = concepts.size();
        if (n == 0) return scores;

        double initialScore = 1.0 / n;
        for (Concept c : concepts) {
            scores.put(c.getId(), initialScore);
        }

        // 构建邻接表
        Map<String, List<String>> outLinks = new HashMap<>();
        for (Concept c : concepts) {
            outLinks.put(c.getId(), new ArrayList<>());
        }
        for (Relationship rel : relationships) {
            String from = rel.getFromConcept().getId();
            String to = rel.getToConcept().getId();
            outLinks.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
        }

        // 迭代 20 轮
        double damping = 0.85;
        for (int iter = 0; iter < 20; iter++) {
            Map<String, Double> newScores = new HashMap<>();
            double totalNew = 0;
            for (Concept c : concepts) {
                double rank = (1 - damping) / n;
                for (Relationship rel : relationships) {
                    if (rel.getToConcept().getId().equals(c.getId())) {
                        String fromId = rel.getFromConcept().getId();
                        int outDegree = outLinks.getOrDefault(fromId, Collections.emptyList()).size();
                        if (outDegree > 0) {
                            rank += damping * scores.getOrDefault(fromId, 0.0) / outDegree;
                        }
                    }
                }
                newScores.put(c.getId(), rank);
                totalNew += rank;
            }
            // 归一化
            for (String id : newScores.keySet()) {
                newScores.put(id, newScores.get(id) / totalNew);
            }
            scores = newScores;
        }

        return scores;
    }

    /**
     * DFS 环路检测
     */
    public List<List<String>> detectCycles(String courseId) {
        List<Concept> concepts = conceptService.getConceptsByCourse(courseId);
        List<Relationship> relationships = relationshipService.getRelationshipsByCourse(courseId);

        Map<String, List<String>> adj = new HashMap<>();
        for (Concept c : concepts) {
            adj.put(c.getId(), new ArrayList<>());
        }
        for (Relationship rel : relationships) {
            adj.get(rel.getFromConcept().getId()).add(rel.getToConcept().getId());
        }

        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();
        List<List<String>> allCycles = new ArrayList<>();

        for (Concept c : concepts) {
            if (!visited.contains(c.getId())) {
                List<String> path = new ArrayList<>();
                dfsCycleDetect(c.getId(), adj, visited, recStack, path, allCycles);
            }
        }

        return allCycles;
    }

    private void dfsCycleDetect(String nodeId, Map<String, List<String>> adj,
                                 Set<String> visited, Set<String> recStack,
                                 List<String> path, List<List<String>> allCycles) {
        visited.add(nodeId);
        recStack.add(nodeId);
        path.add(nodeId);

        for (String neighbor : adj.getOrDefault(nodeId, Collections.emptyList())) {
            if (!visited.contains(neighbor)) {
                dfsCycleDetect(neighbor, adj, visited, recStack, path, allCycles);
            } else if (recStack.contains(neighbor)) {
                // 找到环路
                int startIdx = path.indexOf(neighbor);
                if (startIdx >= 0) {
                    List<String> cycle = new ArrayList<>(path.subList(startIdx, path.size()));
                    cycle.add(neighbor); // 闭合环路
                    allCycles.add(cycle);
                }
            }
        }

        path.remove(path.size() - 1);
        recStack.remove(nodeId);
    }

    /**
     * 查找孤立知识点（没有任何关系的知识点）
     */
    public List<String> findIsolatedConcepts(String courseId) {
        List<Concept> concepts = conceptService.getConceptsByCourse(courseId);
        List<Relationship> relationships = relationshipService.getRelationshipsByCourse(courseId);

        Set<String> connectedIds = new HashSet<>();
        for (Relationship rel : relationships) {
            connectedIds.add(rel.getFromConcept().getId());
            connectedIds.add(rel.getToConcept().getId());
        }

        return concepts.stream()
                .map(Concept::getId)
                .filter(id -> !connectedIds.contains(id))
                .collect(Collectors.toList());
    }

    // ==================== 概念详情与路径 ====================

    /**
     * 获取概念的完整上下文（先修+后续+关联）
     */
    public Map<String, Object> getConceptContext(String conceptId) {
        Map<String, Object> context = new HashMap<>();

        Concept concept = conceptService.findConceptById(conceptId).orElse(null);
        if (concept == null) return context;

        context.put("concept", concept);
        context.put("prerequisites", getDirectPrerequisites(conceptId));
        context.put("dependents", getDirectDependents(conceptId));
        context.put("related", getRelatedByNonPrerequisite(conceptId));
        context.put("chapterTitle", concept.getChapter() != null ? concept.getChapter().getTitle() : null);
        context.put("courseId", concept.getCourse() != null ? concept.getCourse().getId() : null);
        context.put("courseName", concept.getCourse() != null ? concept.getCourse().getName() : null);
        context.put("summary", concept.getSummary());
        context.put("content", concept.getContent());
        context.put("example", concept.getExample());
        context.put("commonPitfall", concept.getCommonPitfall());

        return context;
    }

    private List<Concept> getDirectDependents(String conceptId) {
        return relationshipService.getOutgoingRelationships(conceptId).stream()
                .filter(rel -> rel.getRelationshipType() == Relationship.RelationshipType.PREREQUISITE)
                .map(Relationship::getToConcept)
                .collect(Collectors.toList());
    }

    private List<Concept> getRelatedByNonPrerequisite(String conceptId) {
        List<Relationship> all = new ArrayList<>();
        all.addAll(relationshipService.getOutgoingRelationships(conceptId));
        all.addAll(relationshipService.getIncomingRelationships(conceptId));

        return all.stream()
                .filter(rel -> rel.getRelationshipType() != Relationship.RelationshipType.PREREQUISITE)
                .map(rel -> {
                    if (rel.getFromConcept().getId().equals(conceptId)) {
                        return rel.getToConcept();
                    } else {
                        return rel.getFromConcept();
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取概念的直接先修知识点
     */
    public List<Concept> getDirectPrerequisites(String conceptId) {
        return relationshipService.getIncomingRelationships(conceptId).stream()
                .filter(rel -> rel.getRelationshipType() == Relationship.RelationshipType.PREREQUISITE)
                .map(Relationship::getFromConcept)
                .collect(Collectors.toList());
    }

    // ==================== 递归查询（保留原有方法） ====================

    public List<Concept> getAllPrerequisites(String conceptId) {
        Set<Concept> prerequisites = new HashSet<>();
        findPrerequisitesRecursive(conceptId, prerequisites);
        return new ArrayList<>(prerequisites);
    }

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

    public List<Concept> getAllDependents(String conceptId) {
        Set<Concept> dependents = new HashSet<>();
        findDependentsRecursive(conceptId, dependents);
        return new ArrayList<>(dependents);
    }

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

    public List<Concept> getKeyConcepts(String courseId) {
        return conceptService.getCoreConcepts(courseId, 80);
    }

    // ==================== 学习路径 ====================

    /**
     * 生成个性化学习路径（拓扑排序 + 进度感知）
     */
    public List<Concept> generateLearningPath(String userId, String courseId) {
        List<Concept> allConcepts = conceptService.getConceptsByCourse(courseId);
        List<Relationship> relationships = relationshipService.getRelationshipsByCourse(courseId);
        List<Progress> userProgress = progressService.getUserCourseProgress(userId, courseId);
        List<ConceptProgress> conceptProgressList = conceptProgressService.getUserCourseConceptProgress(userId, courseId);

        Map<String, Progress.ProgressStatus> chapterStatusMap = buildChapterStatusMap(userProgress);
        Map<String, ConceptProgress.ProgressStatus> conceptStatusMap = buildConceptStatusMap(conceptProgressList);

        // 构建入度表（只考虑 PREREQUISITE 关系）
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> adj = new HashMap<>();
        for (Concept c : allConcepts) {
            inDegree.put(c.getId(), 0);
            adj.put(c.getId(), new ArrayList<>());
        }
        for (Relationship rel : relationships) {
            if (rel.getRelationshipType() == Relationship.RelationshipType.PREREQUISITE) {
                String from = rel.getFromConcept().getId();
                String to = rel.getToConcept().getId();
                adj.get(from).add(to);
                inDegree.merge(to, 1, Integer::sum);
            }
        }

        // Kahn 拓扑排序
        Queue<String> queue = new LinkedList<>();
        for (Concept c : allConcepts) {
            if (inDegree.getOrDefault(c.getId(), 0) == 0) {
                queue.add(c.getId());
            }
        }

        Map<String, Concept> conceptMap = allConcepts.stream()
                .collect(Collectors.toMap(Concept::getId, c -> c));

        List<Concept> learningPath = new ArrayList<>();
        while (!queue.isEmpty()) {
            // 按难度和重要性排序队列中的候选
            List<String> candidates = new ArrayList<>(queue);
            queue.clear();
            candidates.sort(Comparator
                    .<String, Integer>comparing(id -> conceptMap.get(id).getDifficultyLevel())
                    .thenComparing(id -> -conceptMap.get(id).getImportanceWeight()));

            for (String id : candidates) {
                Concept concept = conceptMap.get(id);
                if (!isConceptCompletedForPlanning(concept, chapterStatusMap, conceptStatusMap)) {
                    learningPath.add(concept);
                }
                for (String neighbor : adj.getOrDefault(id, Collections.emptyList())) {
                    int newDegree = inDegree.merge(neighbor, -1, Integer::sum);
                    if (newDegree == 0) {
                        queue.add(neighbor);
                    }
                }
            }
        }

        return learningPath;
    }

    /**
     * 检查知识点是否可学习（所有先修已满足）
     */
    public boolean canStudyConcept(String conceptId) {
        List<Relationship> incomingRels = relationshipService.getIncomingRelationships(conceptId);
        for (Relationship rel : incomingRels) {
            if (rel.getRelationshipType() == Relationship.RelationshipType.PREREQUISITE) {
                return false;
            }
        }
        return true;
    }

    /**
     * 根据用户进度判断知识点是否可学习。
     * 规则：所有直接先修知识点所属章节都已完成/掌握，才允许学习。
     */
    public boolean canStudyConcept(String userId, String courseId, String conceptId) {
        if (userId == null || userId.isBlank() || courseId == null || courseId.isBlank()) {
            return canStudyConcept(conceptId);
        }

        List<Concept> prerequisites = getDirectPrerequisites(conceptId);
        if (prerequisites.isEmpty()) {
            return true;
        }

        Map<String, Progress.ProgressStatus> chapterStatusMap =
                buildChapterStatusMap(progressService.getUserCourseProgress(userId, courseId));
        Map<String, ConceptProgress.ProgressStatus> conceptStatusMap =
                buildConceptStatusMap(conceptProgressService.getUserCourseConceptProgress(userId, courseId));

        return prerequisites.stream()
                .allMatch(prerequisite -> isConceptCompletedForPlanning(prerequisite, chapterStatusMap, conceptStatusMap));
    }

    public List<String> getRelatedConcepts(String conceptName, String courseId) {
        if (conceptName == null || conceptName.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String keyword = conceptName.trim().toLowerCase();
        return conceptService.getConceptsByCourse(courseId).stream()
                .map(Concept::getName)
                .filter(name -> name != null && !name.equals(conceptName))
                .filter(name -> name.toLowerCase().contains(keyword))
                .distinct()
                .collect(Collectors.toList());
    }

    // ==================== 私有工具方法 ====================

    private GraphData.GraphNode convertToGraphNode(
            Concept concept,
            Map<String, Progress.ProgressStatus> chapterStatusMap,
            Map<String, ConceptProgress.ProgressStatus> conceptStatusMap
    ) {
        GraphData.GraphNode node = new GraphData.GraphNode(
                concept.getId(),
                concept.getName(),
                concept.getDifficultyLevel(),
                concept.getImportanceWeight()
        );
        node.setDescription(concept.getDescription());
        node.setSummary(concept.getSummary());
        node.setContent(concept.getContent());
        node.setExample(concept.getExample());
        node.setCommonPitfall(concept.getCommonPitfall());
        node.setCategory(difficultyToCategory(concept.getDifficultyLevel()));
        node.setChapterId(concept.getChapter() != null ? concept.getChapter().getId() : null);
        node.setChapterGroupId(concept.getChapter() != null ? concept.getChapter().getId() : null);
        node.setChapterTitle(concept.getChapter() != null ? concept.getChapter().getTitle() : null);
        node.setProgressStatus(resolveConceptProgressStatus(concept, chapterStatusMap, conceptStatusMap));

        return node;
    }

    private GraphData.GraphLink convertToGraphLink(Relationship rel) {
        GraphData.GraphLink link = new GraphData.GraphLink(
                rel.getFromConcept().getId(),
                rel.getToConcept().getId(),
                rel.getRelationshipType().name(),
                rel.getWeight()
        );
        link.setDescription(rel.getDescription());
        return link;
    }

    private GraphData.GraphStatistics buildStatistics(List<Concept> concepts,
                                                       List<Relationship> relationships,
                                                       List<GraphData.GraphNode> nodes) {
        GraphData.GraphStatistics stats = new GraphData.GraphStatistics();
        stats.setTotalNodes(concepts.size());
        stats.setTotalLinks(relationships.size());

        long completed = nodes.stream()
                .filter(n -> "COMPLETED".equals(n.getProgressStatus()) || "MASTERED".equals(n.getProgressStatus()))
                .count();
        long inProgress = nodes.stream().filter(n -> "IN_PROGRESS".equals(n.getProgressStatus())).count();
        stats.setCompletedNodes((int) completed);
        stats.setInProgressNodes((int) inProgress);

        Set<String> connectedIds = new HashSet<>();
        for (Relationship rel : relationships) {
            connectedIds.add(rel.getFromConcept().getId());
            connectedIds.add(rel.getToConcept().getId());
        }
        List<String> isolated = concepts.stream()
                .map(Concept::getId)
                .filter(id -> !connectedIds.contains(id))
                .collect(Collectors.toList());
        stats.setIsolatedNodeIds(isolated);
        stats.setIsolatedCount(isolated.size());
        stats.setChapterGroups(nodes.stream()
                .map(GraphData.GraphNode::getChapterTitle)
                .filter(title -> title != null && !title.isBlank())
                .collect(Collectors.toMap(
                        title -> title,
                        title -> 1,
                        Integer::sum,
                        LinkedHashMap::new
                )));

        return stats;
    }

    private String difficultyToCategory(int level) {
        if (level <= 2) return "基础";
        if (level <= 4) return "进阶";
        return "高级";
    }

    private Map<String, Progress.ProgressStatus> buildChapterStatusMap(List<Progress> progressList) {
        return progressList.stream()
                .filter(progress -> progress.getChapter() != null && progress.getChapter().getId() != null)
                .collect(Collectors.toMap(
                        progress -> progress.getChapter().getId(),
                        Progress::getStatus,
                        (left, right) -> right
                ));
    }

    private Map<String, ConceptProgress.ProgressStatus> buildConceptStatusMap(List<ConceptProgress> progressList) {
        return progressList.stream()
                .filter(progress -> progress.getConcept() != null && progress.getConcept().getId() != null)
                .collect(Collectors.toMap(
                        progress -> progress.getConcept().getId(),
                        ConceptProgress::getStatus,
                        (left, right) -> right
                ));
    }

    private String resolveConceptProgressStatus(
            Concept concept,
            Map<String, Progress.ProgressStatus> chapterStatusMap,
            Map<String, ConceptProgress.ProgressStatus> conceptStatusMap
    ) {
        ConceptProgress.ProgressStatus conceptStatus = conceptStatusMap.get(concept.getId());
        if (conceptStatus != null) {
            return conceptStatus.name();
        }

        String chapterId = concept.getChapter() != null ? concept.getChapter().getId() : null;
        if (chapterId == null) {
            return "NOT_STARTED";
        }

        Progress.ProgressStatus chapterStatus = chapterStatusMap.get(chapterId);
        if (chapterStatus == null) {
            return "NOT_STARTED";
        }
        return switch (chapterStatus) {
            case MASTERED -> "MASTERED";
            case COMPLETED -> "COMPLETED";
            case IN_PROGRESS, REVIEWING -> "IN_PROGRESS";
            default -> "NOT_STARTED";
        };
    }

    private boolean isConceptCompleted(
            Concept concept,
            Map<String, Progress.ProgressStatus> chapterStatusMap,
            Map<String, ConceptProgress.ProgressStatus> conceptStatusMap
    ) {
        String status = resolveConceptProgressStatus(concept, chapterStatusMap, conceptStatusMap);
        return "COMPLETED".equals(status) || "MASTERED".equals(status);
    }

    private boolean isConceptCompletedForPlanning(
            Concept concept,
            Map<String, Progress.ProgressStatus> chapterStatusMap,
            Map<String, ConceptProgress.ProgressStatus> conceptStatusMap
    ) {
        ConceptProgress.ProgressStatus conceptStatus = conceptStatusMap.get(concept.getId());
        if (conceptStatus != null) {
            return conceptStatus == ConceptProgress.ProgressStatus.COMPLETED
                    || conceptStatus == ConceptProgress.ProgressStatus.MASTERED;
        }

        if (!conceptStatusMap.isEmpty()) {
            return false;
        }

        return isConceptCompleted(concept, chapterStatusMap, conceptStatusMap);
    }
}

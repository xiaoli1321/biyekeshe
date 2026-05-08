package com.learnplatform.service;

import com.learnplatform.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 学习路径推荐服务（增强版：Kahn 拓扑排序 + 进度感知）
 */
@Service
@Transactional
public class LearningPathService {

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private RelationshipService relationshipService;

    @Autowired
    private GraphService graphService;

    @Autowired
    private ProgressService progressService;

    /**
     * 为指定用户生成个性化学习路径（Kahn 拓扑排序）
     */
    public LearningPath generatePersonalizedLearningPath(String userId, String courseId) {
        List<Concept> allConcepts = conceptService.getConceptsByCourse(courseId);
        List<Relationship> relationships = relationshipService.getRelationshipsByCourse(courseId);
        List<Progress> userProgress = progressService.getUserCourseProgress(userId, courseId);

        Set<String> completedChapterIds = userProgress.stream()
                .filter(Progress::isCompleted)
                .map(p -> p.getChapter().getId())
                .collect(Collectors.toSet());

        // 构建入度表和邻接表
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> adj = new HashMap<>();
        Map<String, Concept> conceptMap = new HashMap<>();

        for (Concept c : allConcepts) {
            inDegree.put(c.getId(), 0);
            adj.put(c.getId(), new ArrayList<>());
            conceptMap.put(c.getId(), c);
        }

        for (Relationship rel : relationships) {
            if (rel.getRelationshipType() == Relationship.RelationshipType.PREREQUISITE) {
                String from = rel.getFromConcept().getId();
                String to = rel.getToConcept().getId();
                adj.get(from).add(to);
                inDegree.merge(to, 1, Integer::sum);
            }
        }

        // Kahn 算法
        Queue<String> queue = new LinkedList<>();
        for (Concept c : allConcepts) {
            if (inDegree.getOrDefault(c.getId(), 0) == 0) {
                queue.add(c.getId());
            }
        }

        List<Concept> orderedPath = new ArrayList<>();
        Set<String> inPath = new HashSet<>();

        while (!queue.isEmpty()) {
            List<String> candidates = new ArrayList<>(queue);
            queue.clear();
            // 按难度升序、重要程度降序排列同级候选
            candidates.sort(Comparator
                    .<String, Integer>comparing(id -> conceptMap.get(id).getDifficultyLevel())
                    .thenComparing(id -> -conceptMap.get(id).getImportanceWeight()));

            for (String id : candidates) {
                Concept c = conceptMap.get(id);
                String chapterId = c.getChapter() != null ? c.getChapter().getId() : "";

                if (!completedChapterIds.contains(chapterId) && !inPath.contains(id)) {
                    orderedPath.add(c);
                    inPath.add(id);
                }

                for (String neighbor : adj.getOrDefault(id, Collections.emptyList())) {
                    int newDegree = inDegree.merge(neighbor, -1, Integer::sum);
                    if (newDegree == 0) {
                        queue.add(neighbor);
                    }
                }
            }
        }

        int totalConcepts = allConcepts.size();
        int completedConcepts = (int) allConcepts.stream()
                .filter(c -> {
                    String chId = c.getChapter() != null ? c.getChapter().getId() : "";
                    return completedChapterIds.contains(chId);
                })
                .count();
        int remainingConcepts = totalConcepts - completedConcepts;

        return new LearningPath(orderedPath, totalConcepts, completedConcepts, remainingConcepts);
    }

    /**
     * 评估用户知识水平
     */
    public KnowledgeLevel assessKnowledgeLevel(String userId, String courseId) {
        List<Progress> userProgress = progressService.getUserCourseProgress(userId, courseId);

        if (userProgress.isEmpty()) {
            return KnowledgeLevel.BEGINNER;
        }

        long completedChapters = userProgress.stream()
                .filter(Progress::isCompleted)
                .count();
        double avgScore = userProgress.stream()
                .mapToDouble(p -> p.getScore() != null ? p.getScore() : 0.0)
                .average()
                .orElse(0.0);
        double completionRate = userProgress.size() > 0
                ? (completedChapters * 100.0 / userProgress.size())
                : 0;

        if (completionRate >= 90 && avgScore >= 85) {
            return KnowledgeLevel.ADVANCED;
        } else if (completionRate >= 60 && avgScore >= 70) {
            return KnowledgeLevel.INTERMEDIATE;
        } else {
            return KnowledgeLevel.BEGINNER;
        }
    }

    /**
     * 获取学习建议
     */
    public List<String> getLearningSuggestions(String userId, String courseId) {
        List<String> suggestions = new ArrayList<>();
        double completionRate = progressService.calculateCourseCompletionRate(userId, courseId);
        List<Progress> inProgressList = progressService.getInProgressChapters(userId);

        if (completionRate < 30) {
            suggestions.add("建议从课程的初级概念开始学习，循序渐进");
            suggestions.add("每天保持30分钟以上的学习时间");
        } else if (completionRate < 70) {
            suggestions.add("您已经掌握了大部分基础知识，可以尝试一些实践项目");
            suggestions.add("建议定期复习已学过的内容");
        } else {
            suggestions.add("恭喜！您即将完成本课程");
            suggestions.add("考虑学习一些高级课程来进一步提升技能");
        }

        if (!inProgressList.isEmpty()) {
            suggestions.add("继续学习您上次开始的章节");
        }

        return suggestions;
    }

    /**
     * 获取推荐课程
     */
    public List<Course> getRecommendedCourses(String userId) {
        return new ArrayList<>();
    }

    /**
     * 学习路径内部类（增强版）
     */
    public static class LearningPath {
        private List<Concept> pathConcepts;
        private int totalConcepts;
        private int completedConcepts;
        private int remainingConcepts;
        private double completionPercentage;
        private int estimatedTotalMinutes;

        public LearningPath(List<Concept> pathConcepts, int totalConcepts,
                          int completedConcepts, int remainingConcepts) {
            this.pathConcepts = pathConcepts;
            this.totalConcepts = totalConcepts;
            this.completedConcepts = completedConcepts;
            this.remainingConcepts = remainingConcepts;
            this.completionPercentage = totalConcepts > 0
                    ? (completedConcepts * 100.0 / totalConcepts) : 0;
            this.estimatedTotalMinutes = pathConcepts.size() * 30; // 每知识点预估30分钟
        }

        public List<Concept> getPathConcepts() { return pathConcepts; }
        public int getTotalConcepts() { return totalConcepts; }
        public int getCompletedConcepts() { return completedConcepts; }
        public int getRemainingConcepts() { return remainingConcepts; }
        public double getCompletionPercentage() { return completionPercentage; }
        public int getEstimatedTotalMinutes() { return estimatedTotalMinutes; }
        public void setEstimatedTotalMinutes(int estimatedTotalMinutes) { this.estimatedTotalMinutes = estimatedTotalMinutes; }
    }

    public enum KnowledgeLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
}

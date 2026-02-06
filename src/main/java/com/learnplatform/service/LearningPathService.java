package com.learnplatform.service;

import com.learnplatform.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 学习路径推荐服务
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
     * 为用户生成个性化学习路径
     */
    public LearningPath generatePersonalizedLearningPath(String userId, String courseId) {
        // 获取课程所有概念
        List<Concept> allConcepts = conceptService.getConceptsByCourse(courseId);

        // 获取用户学习进度
        List<Progress> userProgress = progressService.getUserCourseProgress(userId, courseId);

        // 分析先修关系，生成学习路径
        List<Concept> learningPath = generateLearningPath(allConcepts, userProgress);

        // 统计信息
        int totalConcepts = allConcepts.size();
        int completedConcepts = userProgress.stream()
                .filter(p -> p.isCompleted())
                .mapToInt(p -> 1)
                .sum();
        int remainingConcepts = totalConcepts - completedConcepts;

        return new LearningPath(learningPath, totalConcepts, completedConcepts, remainingConcepts);
    }

    /**
     * 生成学习路径（基于先修关系和用户进度）
     */
    private List<Concept> generateLearningPath(List<Concept> allConcepts, List<Progress> userProgress) {
        // 构建概念图
        Map<String, Concept> conceptMap = allConcepts.stream()
                .collect(Collectors.toMap(Concept::getId, c -> c));

        // 已完成的章节
        Set<String> completedConceptIds = userProgress.stream()
                .filter(p -> p.isCompleted())
                .map(p -> p.getChapter().getId())
                .collect(Collectors.toSet());

        // 可学习的概念（先修关系已满足）
        List<Concept> learnableConcepts = new ArrayList<>();
        Set<String> inPath = new HashSet<>();

        // 拓扑排序实现
        while (true) {
            Concept nextConcept = findNextLearnableConcept(allConcepts, completedConceptIds, inPath);
            if (nextConcept == null) break;

            learnableConcepts.add(nextConcept);
            inPath.add(nextConcept.getId());
        }

        return learnableConcepts;
    }

    /**
     * 查找下一个可学习的概念
     */
    private Concept findNextLearnableConcept(List<Concept> concepts,
                                           Set<String> completedConceptIds,
                                           Set<String> inPath) {
        // 为每个概念打分（综合考虑难度、重要性和先修关系）
        Map<Concept, Double> conceptScores = new HashMap<>();

        for (Concept concept : concepts) {
            if (inPath.contains(concept.getId())) continue;

            double score = 0.0;

            // 难度分数（较容易的概念分数更高）
            score += (6 - concept.getDifficultyLevel()) * 10;

            // 重要程度分数
            score += concept.getImportanceWeight() / 10.0;

            // 先修关系检查
            boolean prerequisitesMet = checkPrerequisitesMet(concept, completedConceptIds, inPath);
            if (prerequisitesMet) {
                score += 20; // 可学习的概念额外加分
            }

            conceptScores.put(concept, score);
        }

        // 返回分数最高的概念
        return conceptScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * 检查先修关系是否满足
     */
    private boolean checkPrerequisitesMet(Concept concept, Set<String> completedConceptIds, Set<String> inPath) {
        // 获取概念的直接先修知识点
        List<Relationship> prerequisites = relationshipService.getIncomingRelationships(concept.getId())
                .stream()
                .filter(rel -> rel.getRelationshipType() == Relationship.RelationshipType.PREREQUISITE)
                .collect(Collectors.toList());

        if (prerequisites.isEmpty()) {
            return true; // 没有先修关系，可以直接学习
        }

        // 检查所有先修知识点是否已完成或在当前路径中
        for (Relationship prerequisite : prerequisites) {
            String prerequisiteId = prerequisite.getFromConcept().getId();
            // 如果先修知识点不在已完成列表中也不在当前路径中，则不可学习
            if (!completedConceptIds.contains(prerequisiteId) && !inPath.contains(prerequisiteId)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取推荐课程
     */
    public List<Course> getRecommendedCourses(String userId) {
        // 简化实现：返回所有已发布的课程
        // 在实际应用中，这里应该基于用户的学习历史、兴趣和进度进行推荐
        return new ArrayList<>(); // 实际应用中应由CourseService返回
    }

    /**
     * 评估用户的知识水平
     */
    public KnowledgeLevel assessKnowledgeLevel(String userId, String courseId) {
        List<Progress> userProgress = progressService.getUserCourseProgress(userId, courseId);

        if (userProgress.isEmpty()) {
            return KnowledgeLevel.BEGINNER;
        }

        // 计算平均难度
        double avgDifficulty = userProgress.stream()
                .filter(p -> p.isCompleted())
                .mapToDouble(p -> p.getChapter().getOrderIndex() * 1.0)
                .average()
                .orElse(0.0);

        // 计算得分
        double avgScore = userProgress.stream()
                .mapToDouble(Progress::getScore)
                .average()
                .orElse(0.0);

        // 计算完成率
        long totalChapters = userProgress.size();
        long completedChapters = userProgress.stream()
                .filter(Progress::isCompleted)
                .mapToInt(p -> 1)
                .sum();
        double completionRate = (completedChapters * 100.0) / totalChapters;

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

        ProgressService progressService = this.progressService;
        double completionRate = progressService.calculateCourseCompletionRate(userId, courseId);
        List<Progress> inProgressList = progressService.getInProgressChapters(userId);

        if (completionRate < 30) {
            suggestions.add("建议从课程的初级概念开始学习，循序渐进");
            suggestions.add("每天保持30分钟以上 的学习时间");
        } else if (completionRate < 70) {
            suggestions.add("您已经掌握了大部分基础知识，可以尝试一些实践项目");
            suggestions.add("建议定期复习已学过的内容");
        } else {
            suggestions.add("恭喜！您即将完成本课程");
            suggestions.add("考虑一下学习一些高级课程来进一步提升技能");
        }

        if (!inProgressList.isEmpty()) {
            Progress lastProgress = inProgressList.get(0);
            suggestions.add("继续学习您上次开始的章节");
        }

        return suggestions;
    }

    /**
     * 学习路径内部类
     */
    public static class LearningPath {
        private List<Concept> pathConcepts;
        private int totalConcepts;
        private int completedConcepts;
        private int remainingConcepts;
        private double completionPercentage;

        public LearningPath(List<Concept> pathConcepts, int totalConcepts,
                          int completedConcepts, int remainingConcepts) {
            this.pathConcepts = pathConcepts;
            this.totalConcepts = totalConcepts;
            this.completedConcepts = completedConcepts;
            this.remainingConcepts = remainingConcepts;
            this.completionPercentage = totalConcepts > 0
                    ? (completedConcepts * 100.0 / totalConcepts) : 0;
        }

        // Getters
        public List<Concept> getPathConcepts() { return pathConcepts; }
        public int getTotalConcepts() { return totalConcepts; }
        public int getCompletedConcepts() { return completedConcepts; }
        public int getRemainingConcepts() { return remainingConcepts; }
        public double getCompletionPercentage() { return completionPercentage; }
    }

    /**
     * 知识水平枚举
     */
    public enum KnowledgeLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
}
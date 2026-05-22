package com.learnplatform.dto;

import java.util.List;
import java.util.Map;

/**
 * 知识图谱数据传输对象（增强版）
 */
public class GraphData {
    private List<GraphNode> nodes;
    private List<GraphLink> links;
    private GraphStatistics statistics;

    public GraphData() {}

    public GraphData(List<GraphNode> nodes, List<GraphLink> links) {
        this.nodes = nodes;
        this.links = links;
    }

    public GraphData(List<GraphNode> nodes, List<GraphLink> links, GraphStatistics statistics) {
        this.nodes = nodes;
        this.links = links;
        this.statistics = statistics;
    }

    public List<GraphNode> getNodes() { return nodes; }
    public void setNodes(List<GraphNode> nodes) { this.nodes = nodes; }
    public List<GraphLink> getLinks() { return links; }
    public void setLinks(List<GraphLink> links) { this.links = links; }
    public GraphStatistics getStatistics() { return statistics; }
    public void setStatistics(GraphStatistics statistics) { this.statistics = statistics; }

    /**
     * 增强版图谱节点
     */
    public static class GraphNode {
        private String id;
        private String name;
        private String description;
        private String summary;
        private String content;
        private String example;
        private String commonPitfall;
        private int difficultyLevel;
        private int importanceWeight;
        private String category;          // 知识点类别标签
        private String progressStatus;    // NOT_STARTED / IN_PROGRESS / COMPLETED / MASTERED
        private String chapterId;         // 所属章节ID（用于进度更新）
        private String chapterGroupId;    // 图谱章节分组ID
        private String chapterTitle;      // 所属章节名称
        private Integer resourceCount;    // 关联学习资源数
        private Double centrality;        // 中心度分数（可选）

        public GraphNode() {}

        public GraphNode(String id, String name, int difficultyLevel, int importanceWeight) {
            this.id = id;
            this.name = name;
            this.difficultyLevel = difficultyLevel;
            this.importanceWeight = importanceWeight;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getExample() { return example; }
        public void setExample(String example) { this.example = example; }
        public String getCommonPitfall() { return commonPitfall; }
        public void setCommonPitfall(String commonPitfall) { this.commonPitfall = commonPitfall; }
        public int getDifficultyLevel() { return difficultyLevel; }
        public void setDifficultyLevel(int difficultyLevel) { this.difficultyLevel = difficultyLevel; }
        public int getImportanceWeight() { return importanceWeight; }
        public void setImportanceWeight(int importanceWeight) { this.importanceWeight = importanceWeight; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getProgressStatus() { return progressStatus; }
        public void setProgressStatus(String progressStatus) { this.progressStatus = progressStatus; }
        public String getChapterId() { return chapterId; }
        public void setChapterId(String chapterId) { this.chapterId = chapterId; }
        public String getChapterGroupId() { return chapterGroupId; }
        public void setChapterGroupId(String chapterGroupId) { this.chapterGroupId = chapterGroupId; }
        public String getChapterTitle() { return chapterTitle; }
        public void setChapterTitle(String chapterTitle) { this.chapterTitle = chapterTitle; }
        public Integer getResourceCount() { return resourceCount; }
        public void setResourceCount(Integer resourceCount) { this.resourceCount = resourceCount; }
        public Double getCentrality() { return centrality; }
        public void setCentrality(Double centrality) { this.centrality = centrality; }
    }

    /**
     * 增强版图谱边
     */
    public static class GraphLink {
        private String source;
        private String target;
        private String type;
        private double weight;
        private String description;

        public GraphLink() {}

        public GraphLink(String source, String target, String type, double weight) {
            this.source = source;
            this.target = target;
            this.type = type;
            this.weight = weight;
        }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public double getWeight() { return weight; }
        public void setWeight(double weight) { this.weight = weight; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * 图谱统计信息
     */
    public static class GraphStatistics {
        private int totalNodes;
        private int totalLinks;
        private int completedNodes;
        private int inProgressNodes;
        private List<String> isolatedNodeIds;
        private List<List<String>> cycles;           // 检测到的环路
        private Map<String, Double> centralityMap;    // 节点ID -> 中心度
        private Map<String, Integer> chapterGroups;   // 章节名 -> 节点数量
        private boolean hasCycles;
        private int isolatedCount;

        public int getTotalNodes() { return totalNodes; }
        public void setTotalNodes(int totalNodes) { this.totalNodes = totalNodes; }
        public int getTotalLinks() { return totalLinks; }
        public void setTotalLinks(int totalLinks) { this.totalLinks = totalLinks; }
        public int getCompletedNodes() { return completedNodes; }
        public void setCompletedNodes(int completedNodes) { this.completedNodes = completedNodes; }
        public int getInProgressNodes() { return inProgressNodes; }
        public void setInProgressNodes(int inProgressNodes) { this.inProgressNodes = inProgressNodes; }
        public List<String> getIsolatedNodeIds() { return isolatedNodeIds; }
        public void setIsolatedNodeIds(List<String> isolatedNodeIds) { this.isolatedNodeIds = isolatedNodeIds; }
        public List<List<String>> getCycles() { return cycles; }
        public void setCycles(List<List<String>> cycles) { this.cycles = cycles; }
        public Map<String, Double> getCentralityMap() { return centralityMap; }
        public void setCentralityMap(Map<String, Double> centralityMap) { this.centralityMap = centralityMap; }
        public Map<String, Integer> getChapterGroups() { return chapterGroups; }
        public void setChapterGroups(Map<String, Integer> chapterGroups) { this.chapterGroups = chapterGroups; }
        public boolean isHasCycles() { return hasCycles; }
        public void setHasCycles(boolean hasCycles) { this.hasCycles = hasCycles; }
        public int getIsolatedCount() { return isolatedCount; }
        public void setIsolatedCount(int isolatedCount) { this.isolatedCount = isolatedCount; }
    }
}

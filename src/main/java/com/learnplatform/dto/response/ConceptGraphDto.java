package com.learnplatform.dto.response;

import java.util.List;

/**
 * 知识图谱响应DTO
 */
public class ConceptGraphDto {
    private List<ConceptNodeDto> nodes;
    private List<RelationEdgeDto> edges;

    public ConceptGraphDto() {
    }

    public ConceptGraphDto(List<ConceptNodeDto> nodes, List<RelationEdgeDto> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    // Getters and Setters
    public List<ConceptNodeDto> getNodes() {
        return nodes;
    }

    public void setNodes(List<ConceptNodeDto> nodes) {
        this.nodes = nodes;
    }

    public List<RelationEdgeDto> getEdges() {
        return edges;
    }

    public void setEdges(List<RelationEdgeDto> edges) {
        this.edges = edges;
    }

    // 内部静态类：概念节点
    public static class ConceptNodeDto {
        private String id;
        private String label;
        private String description;
        private String category;
        private int level;

        public ConceptNodeDto() {
        }

        public ConceptNodeDto(String id, String label, String description, String category, int level) {
            this.id = id;
            this.label = label;
            this.description = description;
            this.category = category;
            this.level = level;
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }
    }

    // 内部静态类：关系边
    public static class RelationEdgeDto {
        private String id;
        private String source;
        private String target;
        private String label;
        private String type;

        public RelationEdgeDto() {
        }

        public RelationEdgeDto(String id, String source, String target, String label, String type) {
            this.id = id;
            this.source = source;
            this.target = target;
            this.label = label;
            this.type = type;
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
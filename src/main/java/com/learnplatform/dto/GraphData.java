package com.learnplatform.dto;

import java.util.List;

/**
 * 知识图谱数据传输对象
 */
public class GraphData {
    private List<Node> nodes;
    private List<Link> links;

    public GraphData() {}

    public GraphData(List<Node> nodes, List<Link> links) {
        this.nodes = nodes;
        this.links = links;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public static class Node {
        private String id;
        private String name;
        private int level;
        private int weight;

        public Node() {}

        public Node(String id, String name, int level, int weight) {
            this.id = id;
            this.name = name;
            this.level = level;
            this.weight = weight;
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }
    }

    public static class Link {
        private String source;
        private String target;
        private String type;
        private double weight;

        public Link() {}

        public Link(String source, String target, String type, double weight) {
            this.source = source;
            this.target = target;
            this.type = type;
            this.weight = weight;
        }

        // Getters and Setters
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

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }
    }
}
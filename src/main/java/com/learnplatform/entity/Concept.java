package com.learnplatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 知识点实体类（知识图谱节点）
 */
@Document(collection = "concepts")
public class Concept {

    @Id
    private String id;

    @NotNull
    @DBRef
    private Course course;

    @JsonIgnore
    @DBRef
    private Chapter chapter;

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    private String description;

    // 难度等级（1-5）
    private Integer difficultyLevel = 1;

    // 重要程度权重（0-100）
    private Integer importanceWeight = 50;

    @CreatedDate
    private LocalDateTime createdAt;

    // 作为源节点的关系（一对多）
    @DBRef
    private List<Relationship> outgoingRelationships = new ArrayList<>();

    // 作为目标节点的关系（一对多）
    @DBRef
    private List<Relationship> incomingRelationships = new ArrayList<>();

    // 默认构造函数
    public Concept() {
    }

    // 构造函数
    public Concept(Course course, Chapter chapter, String name, String description) {
        this.course = course;
        this.chapter = chapter;
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(Integer difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public Integer getImportanceWeight() {
        return importanceWeight;
    }

    public void setImportanceWeight(Integer importanceWeight) {
        this.importanceWeight = importanceWeight;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Relationship> getOutgoingRelationships() {
        return outgoingRelationships;
    }

    public void setOutgoingRelationships(List<Relationship> outgoingRelationships) {
        this.outgoingRelationships = outgoingRelationships;
    }

    public List<Relationship> getIncomingRelationships() {
        return incomingRelationships;
    }

    public void setIncomingRelationships(List<Relationship> incomingRelationships) {
        this.incomingRelationships = incomingRelationships;
    }

    // 便利方法：获取所有先修知识点
    public List<Concept> getPrerequisites() {
        return incomingRelationships.stream()
                .filter(rel -> rel.getRelationshipType() == Relationship.RelationshipType.PREREQUISITE)
                .map(Relationship::getFromConcept)
                .toList();
    }

    // 便利方法：获取所有依赖此知识点的概念
    public List<Concept> getDependents() {
        return outgoingRelationships.stream()
                .filter(rel -> rel.getRelationshipType() == Relationship.RelationshipType.PREREQUISITE)
                .map(Relationship::getToConcept)
                .toList();
    }

    @Override
    public String toString() {
        return "Concept{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", difficultyLevel=" + difficultyLevel +
                ", importanceWeight=" + importanceWeight +
                '}';
    }
}
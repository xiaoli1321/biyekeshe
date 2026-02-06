package com.learnplatform.entity;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 关系实体类（知识图谱边）
 */
@Document(collection = "relationships")
public class Relationship {

    @Id
    private String id;

    @NotNull
    @DBRef
    private Concept fromConcept;

    @NotNull
    @DBRef
    private Concept toConcept;

    @NotNull
    private RelationshipType relationshipType;

    // 关系的权重/强度
    private Double weight = 1.0;

    // 关系描述
    private String description;

    @CreatedDate
    private LocalDateTime createdAt;

    public enum RelationshipType {
        PREREQUISITE,      // 先修关系
        DEPENDS_ON,        // 依赖关系
        USES,             // 使用关系
        SIMILAR_TO,       // 相似关系
        PART_OF,          // 包含关系
        SYNONYM,          // 同义词
        RECOMMENDS        // 推荐关系
    }

    // 默认构造函数
    public Relationship() {
    }

    // 构造函数
    public Relationship(Concept fromConcept, Concept toConcept, RelationshipType relationshipType) {
        this.fromConcept = fromConcept;
        this.toConcept = toConcept;
        this.relationshipType = relationshipType;
    }

    // 全参数构造函数
    public Relationship(Concept fromConcept, Concept toConcept, RelationshipType relationshipType,
                       Double weight, String description) {
        this.fromConcept = fromConcept;
        this.toConcept = toConcept;
        this.relationshipType = relationshipType;
        this.weight = weight;
        this.description = description;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Concept getFromConcept() {
        return fromConcept;
    }

    public void setFromConcept(Concept fromConcept) {
        this.fromConcept = fromConcept;
    }

    public Concept getToConcept() {
        return toConcept;
    }

    public void setToConcept(Concept toConcept) {
        this.toConcept = toConcept;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Relationship{" +
                "id=" + id +
                ", fromConcept=" + (fromConcept != null ? fromConcept.getName() : "null") +
                ", toConcept=" + (toConcept != null ? toConcept.getName() : "null") +
                ", relationshipType=" + relationshipType +
                ", weight=" + weight +
                ", description='" + description + '\'' +
                '}';
    }
}
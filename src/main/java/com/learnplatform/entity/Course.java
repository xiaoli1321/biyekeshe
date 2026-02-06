package com.learnplatform.entity;

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
 * 课程实体类
 */
@Document(collection = "courses")
public class Course {

    @Id
    private String id;

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    private String description;

    @NotNull
    private DifficultyLevel difficultyLevel = DifficultyLevel.BEGINNER;

    @CreatedDate
    private LocalDateTime createdAt;

    private boolean published = false;

    // 课程封面图片URL
    private String coverImageUrl;

    // 讲师信息
    private String instructor;

    // 预估学习时长（小时）
    private Integer estimatedHours;

    // 标签
    private String tags;

    // 关联章节（一对多）
    @DBRef
    private List<Chapter> chapters = new ArrayList<>();

    public enum DifficultyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    // 默认构造函数
    public Course() {
    }

    // 构造函数
    public Course(String name, String description, DifficultyLevel difficultyLevel) {
        this.name = name;
        this.description = description;
        this.difficultyLevel = difficultyLevel;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public Integer getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Integer estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    // 便利方法
    public void addChapter(Chapter chapter) {
        chapters.add(chapter);
        chapter.setCourse(this);
    }

    public void removeChapter(Chapter chapter) {
        chapters.remove(chapter);
        chapter.setCourse(null);
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", difficultyLevel=" + difficultyLevel +
                ", createdAt=" + createdAt +
                ", published=" + published +
                ", instructor='" + instructor + '\'' +
                ", estimatedHours=" + estimatedHours +
                ", tags='" + tags + '\'' +
                ", chapters count=" + chapters.size() +
                '}';
    }
}
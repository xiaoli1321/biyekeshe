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
 * 章节实体类
 */
@Document(collection = "chapters")
public class Chapter {

    @Id
    private String id;

    @NotNull
    @DBRef
    private Course course;

    @NotBlank
    @Size(min = 2, max = 200)
    private String title;

    private String content;

    // 章节顺序
    private Integer orderIndex = 0;

    @CreatedDate
    private LocalDateTime createdAt;

    // 章节类型（文本、视频、练习等）
    private ChapterType type = ChapterType.TEXT;

    // 预估完成时间（分钟）
    private Integer estimatedMinutes;

    // 视频地址
    private String videoUrl;

    // 附件列表（资源）
    private List<String> attachmentUrls = new ArrayList<>();

    // 是否可跳过
    private boolean skippable = false;

    // 知识点关联（一对多）
    @DBRef
    private List<Concept> concepts = new ArrayList<>();

    // 学习进度关联（一对多）
    @DBRef
    private List<Progress> progressList = new ArrayList<>();

    public enum ChapterType {
        TEXT, VIDEO, QUIZ, EXERCISE, PROJECT
    }

    // 默认构造函数
    public Chapter() {
    }

    // 构造函数
    public Chapter(Course course, String title, String content, Integer orderIndex) {
        this.course = course;
        this.title = title;
        this.content = content;
        this.orderIndex = orderIndex;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ChapterType getType() {
        return type;
    }

    public void setType(ChapterType type) {
        this.type = type;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(Integer estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public List<String> getAttachmentUrls() {
        return attachmentUrls;
    }

    public void setAttachmentUrls(List<String> attachmentUrls) {
        this.attachmentUrls = attachmentUrls;
    }

    public boolean isSkippable() {
        return skippable;
    }

    public void setSkippable(boolean skippable) {
        this.skippable = skippable;
    }

    public List<Concept> getConcepts() {
        return concepts;
    }

    public void setConcepts(List<Concept> concepts) {
        this.concepts = concepts;
    }

    public List<Progress> getProgressList() {
        return progressList;
    }

    public void setProgressList(List<Progress> progressList) {
        this.progressList = progressList;
    }

    // Convenience methods for DTO conversion
    public String getCourseId() {
        return course != null ? course.getId() : null;
    }

    public String getDescription() {
        // Use title as description if no description field exists
        return title;
    }

    // 便利方法
    public void addConcept(Concept concept) {
        concepts.add(concept);
        concept.setChapter(this);
    }

    public void removeConcept(Concept concept) {
        concepts.remove(concept);
        concept.setChapter(null);
    }

    @Override
    public String toString() {
        return "Chapter{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", orderIndex=" + orderIndex +
                ", type=" + type +
                ", estimatedMinutes=" + estimatedMinutes +
                ", skippable=" + skippable +
                '}';
    }
}
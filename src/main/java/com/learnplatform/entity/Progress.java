package com.learnplatform.entity;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 学习进度实体类
 */
@Document(collection = "progress")
@CompoundIndex(name = "user_chapter_idx", def = "{'user': 1, 'chapter': 1}", unique = true)
public class Progress {

    @Id
    private String id;

    @NotNull
    @DBRef
    private User user;

    @NotNull
    @DBRef
    private Course course;

    @NotNull
    @DBRef
    private Chapter chapter;

    @CreatedDate
    private LocalDateTime startedAt;

    // 完成时间
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 完成时间(null表示未完成)
    private LocalDateTime completedAt;

    // 学习时长（分钟）
    private Integer studyDurationMinutes;

    // 学习得分（0-100）
    private Double score = 0.0;

    // 状态
    private ProgressStatus status = ProgressStatus.NOT_STARTED;

    // 学习笔记
    private String notes;

    // 标记（用户自定义标签）
    private String tags;

    public enum ProgressStatus {
        NOT_STARTED,    // 未开始
        IN_PROGRESS,    // 进行中
        COMPLETED,      // 已完成
        REVIEWING,      // 复习中
        MASTERED        // 已掌握
    }

    // 默认构造函数
    public Progress() {
    }

    // 构造函数
    public Progress(User user, Course course, Chapter chapter) {
        this.user = user;
        this.course = course;
        this.chapter = chapter;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getStudyDurationMinutes() {
        return studyDurationMinutes;
    }

    public void setStudyDurationMinutes(Integer studyDurationMinutes) {
        this.studyDurationMinutes = studyDurationMinutes;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public ProgressStatus getStatus() {
        return status;
    }

    public void setStatus(ProgressStatus status) {
        this.status = status;
        // 自动更新完成时间
        if (status == ProgressStatus.COMPLETED && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    // 便利方法
    public boolean isCompleted() {
        return status == ProgressStatus.COMPLETED || status == ProgressStatus.MASTERED;
    }

    @Override
    public String toString() {
        return "Progress{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : "null") +
                ", courseId=" + (course != null ? course.getId() : "null") +
                ", chapterId=" + (chapter != null ? chapter.getId() : "null") +
                ", status=" + status +
                ", score=" + score +
                ", isCompleted=" + isCompleted() +
                '}';
    }
}
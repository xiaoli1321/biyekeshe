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
 * 知识点学习进度实体类
 */
@Document(collection = "concept_progress")
@CompoundIndex(name = "user_concept_idx", def = "{'user': 1, 'concept': 1}", unique = true)
public class ConceptProgress {

    @Id
    private String id;

    @NotNull
    @DBRef
    private User user;

    @NotNull
    @DBRef
    private Course course;

    @DBRef
    private Chapter chapter;

    @NotNull
    @DBRef
    private Concept concept;

    @CreatedDate
    private LocalDateTime startedAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    private Integer studyDurationMinutes = 0;

    private ProgressStatus status = ProgressStatus.NOT_STARTED;

    public enum ProgressStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED,
        MASTERED
    }

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

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
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

    public ProgressStatus getStatus() {
        return status;
    }

    public void setStatus(ProgressStatus status) {
        this.status = status;
        if ((status == ProgressStatus.COMPLETED || status == ProgressStatus.MASTERED) && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public boolean isCompleted() {
        return status == ProgressStatus.COMPLETED || status == ProgressStatus.MASTERED;
    }
}

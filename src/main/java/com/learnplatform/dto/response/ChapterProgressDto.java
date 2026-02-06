package com.learnplatform.dto.response;

import com.learnplatform.entity.Progress;

import java.time.LocalDateTime;

/**
 * 章节学习进度响应
 */
public class ChapterProgressDto {
    private String chapterId;
    private Progress.ProgressStatus status;
    private boolean completed;
    private Integer studyMinutes;
    private LocalDateTime completedAt;

    public ChapterProgressDto() {
    }

    public ChapterProgressDto(String chapterId, Progress.ProgressStatus status, boolean completed,
                              Integer studyMinutes, LocalDateTime completedAt) {
        this.chapterId = chapterId;
        this.status = status;
        this.completed = completed;
        this.studyMinutes = studyMinutes;
        this.completedAt = completedAt;
    }

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public Progress.ProgressStatus getStatus() {
        return status;
    }

    public void setStatus(Progress.ProgressStatus status) {
        this.status = status;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Integer getStudyMinutes() {
        return studyMinutes;
    }

    public void setStudyMinutes(Integer studyMinutes) {
        this.studyMinutes = studyMinutes;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}

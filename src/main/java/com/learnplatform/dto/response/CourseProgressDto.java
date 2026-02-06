package com.learnplatform.dto.response;

import java.util.List;

/**
 * 课程学习进度响应
 */
public class CourseProgressDto {
    private String courseId;
    private long totalChapters;
    private long completedChapters;
    private double completionRate;
    private List<String> completedChapterIds;
    private List<String> inProgressChapterIds;

    public CourseProgressDto() {
    }

    public CourseProgressDto(String courseId, long totalChapters, long completedChapters,
                             double completionRate, List<String> completedChapterIds,
                             List<String> inProgressChapterIds) {
        this.courseId = courseId;
        this.totalChapters = totalChapters;
        this.completedChapters = completedChapters;
        this.completionRate = completionRate;
        this.completedChapterIds = completedChapterIds;
        this.inProgressChapterIds = inProgressChapterIds;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public long getTotalChapters() {
        return totalChapters;
    }

    public void setTotalChapters(long totalChapters) {
        this.totalChapters = totalChapters;
    }

    public long getCompletedChapters() {
        return completedChapters;
    }

    public void setCompletedChapters(long completedChapters) {
        this.completedChapters = completedChapters;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }

    public List<String> getCompletedChapterIds() {
        return completedChapterIds;
    }

    public void setCompletedChapterIds(List<String> completedChapterIds) {
        this.completedChapterIds = completedChapterIds;
    }

    public List<String> getInProgressChapterIds() {
        return inProgressChapterIds;
    }

    public void setInProgressChapterIds(List<String> inProgressChapterIds) {
        this.inProgressChapterIds = inProgressChapterIds;
    }
}

package com.learnplatform.dto.response;

import java.util.List;

public class DashboardStatsDto {
    private long totalCourses;
    private long completedChapters;
    private int totalHours;
    private List<CourseDto> recommendedCourses;

    public DashboardStatsDto() {
    }

    public DashboardStatsDto(long totalCourses, long completedChapters, int totalHours) {
        this.totalCourses = totalCourses;
        this.completedChapters = completedChapters;
        this.totalHours = totalHours;
    }

    public DashboardStatsDto(long totalCourses, long completedChapters, int totalHours,
                           List<CourseDto> recommendedCourses) {
        this(totalCourses, completedChapters, totalHours);
        this.recommendedCourses = recommendedCourses;
    }

    // Getters and Setters
    public long getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(long totalCourses) {
        this.totalCourses = totalCourses;
    }

    public long getCompletedChapters() {
        return completedChapters;
    }

    public void setCompletedChapters(long completedChapters) {
        this.completedChapters = completedChapters;
    }

    public int getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(int totalHours) {
        this.totalHours = totalHours;
    }

    public List<CourseDto> getRecommendedCourses() {
        return recommendedCourses;
    }

    public void setRecommendedCourses(List<CourseDto> recommendedCourses) {
        this.recommendedCourses = recommendedCourses;
    }
}
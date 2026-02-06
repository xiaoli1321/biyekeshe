package com.learnplatform.dto.response;

import java.util.List;

public class LearningPathDto {
    private String courseId;
    private String courseName;
    private List<ChapterDto> path;
    private List<String> suggestions;

    public LearningPathDto() {
    }

    public LearningPathDto(String courseId, String courseName, List<ChapterDto> path, List<String> suggestions) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.path = path;
        this.suggestions = suggestions;
    }

    // Getters and Setters
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public List<ChapterDto> getPath() {
        return path;
    }

    public void setPath(List<ChapterDto> path) {
        this.path = path;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
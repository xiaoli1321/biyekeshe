package com.learnplatform.dto.request;

import com.learnplatform.entity.Course;

public class CourseSearchRequest {
    private String keyword;
    private Course.DifficultyLevel difficulty;
    private int page = 0;
    private int size = 12;

    public CourseSearchRequest() {
    }

    public CourseSearchRequest(String keyword, Course.DifficultyLevel difficulty, int page, int size) {
        this.keyword = keyword;
        this.difficulty = difficulty;
        this.page = page;
        this.size = size;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Course.DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Course.DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
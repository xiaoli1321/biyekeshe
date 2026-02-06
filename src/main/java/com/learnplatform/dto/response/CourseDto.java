package com.learnplatform.dto.response;

import com.learnplatform.entity.Course;

import java.util.List;
import java.util.stream.Collectors;

public class CourseDto {
    private String id;
    private String name;
    private String description;
    private String instructor;
    private Course.DifficultyLevel difficultyLevel;
    private Integer estimatedHours;
    private String tags;
    private boolean published;
    private List<ChapterDto> chapters;

    public CourseDto() {
    }

    public CourseDto(String id, String name, String description, String instructor,
                     Course.DifficultyLevel difficultyLevel, Integer estimatedHours,
                     String tags, boolean published, List<ChapterDto> chapters) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.instructor = instructor;
        this.difficultyLevel = difficultyLevel;
        this.estimatedHours = estimatedHours;
        this.tags = tags;
        this.published = published;
        this.chapters = chapters;
    }

    // 从Course实体转换
    public static CourseDto fromCourse(Course course) {
        if (course == null) {
            return null;
        }

        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        dto.setInstructor(course.getInstructor());
        dto.setDifficultyLevel(course.getDifficultyLevel());
        dto.setEstimatedHours(course.getEstimatedHours());
        dto.setTags(course.getTags());
        dto.setPublished(course.isPublished());

        if (course.getChapters() != null) {
            dto.setChapters(course.getChapters().stream()
                    .map(ChapterDto::fromChapter)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    // 从课程（不包含章节）转换
    public static CourseDto fromCourseBasic(Course course) {
        if (course == null) {
            return null;
        }

        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        dto.setInstructor(course.getInstructor());
        dto.setDifficultyLevel(course.getDifficultyLevel());
        dto.setEstimatedHours(course.getEstimatedHours());
        dto.setTags(course.getTags());
        dto.setPublished(course.isPublished());

        return dto;
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

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public Course.DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(Course.DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
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

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public List<ChapterDto> getChapters() {
        return chapters;
    }

    public void setChapters(List<ChapterDto> chapters) {
        this.chapters = chapters;
    }
}
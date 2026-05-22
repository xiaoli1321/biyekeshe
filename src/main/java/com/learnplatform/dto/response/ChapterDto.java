package com.learnplatform.dto.response;

import com.learnplatform.entity.Chapter;

import java.util.List;

public class ChapterDto {
    private String id;
    private String courseId;
    private String title;
    private String description;
    private String content;
    private int orderIndex;
    private int estimatedMinutes;
    private String videoUrl;
    private List<String> attachmentUrls;
    private String type;

    public ChapterDto() {
    }

    public ChapterDto(String id, String courseId, String title, String description,
                      String content, int orderIndex, int estimatedMinutes,
                      String videoUrl, List<String> attachmentUrls, String type) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.content = content;
        this.orderIndex = orderIndex;
        this.estimatedMinutes = estimatedMinutes;
        this.videoUrl = videoUrl;
        this.attachmentUrls = attachmentUrls;
        this.type = type;
    }

    // 从Chapter实体转换
    public static ChapterDto fromChapter(Chapter chapter) {
        if (chapter == null) {
            return null;
        }

        return new ChapterDto(
                chapter.getId(),
                chapter.getCourseId(),
                chapter.getTitle(),
                chapter.getDescription(),
                chapter.getContent(),
                chapter.getOrderIndex() != null ? chapter.getOrderIndex() : 0,
                chapter.getEstimatedMinutes() != null ? chapter.getEstimatedMinutes() : 0,
                chapter.getVideoUrl(),
                chapter.getAttachmentUrls(),
                chapter.getType() != null ? chapter.getType().name() : "TEXT"
        );
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(int estimatedMinutes) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

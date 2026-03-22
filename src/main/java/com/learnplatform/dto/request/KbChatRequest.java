package com.learnplatform.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 知识库聊天请求 DTO
 */
public class KbChatRequest {

    @NotBlank(message = "消息内容不能为空")
    private String message;
    private String collectionId;

    public KbChatRequest() {
    }

    public KbChatRequest(String message) {
        this.message = message;
    }

    public KbChatRequest(String message, String collectionId) {
        this.message = message;
        this.collectionId = collectionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }
}

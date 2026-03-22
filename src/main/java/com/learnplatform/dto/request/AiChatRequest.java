package com.learnplatform.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * AI 聊天请求 DTO
 */
public class AiChatRequest {

    @NotBlank(message = "章节ID不能为空")
    private String chapterId;

    @NotBlank(message = "消息内容不能为空")
    private String message;

    public AiChatRequest() {
    }

    public AiChatRequest(String chapterId, String message) {
        this.chapterId = chapterId;
        this.message = message;
    }

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

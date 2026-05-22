package com.learnplatform.dto.request;

import com.learnplatform.dto.workflow.HistoryMessage;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 聊天请求 DTO
 */
public class AiChatRequest {

    @NotBlank(message = "章节ID不能为空")
    private String chapterId;

    @NotBlank(message = "消息内容不能为空")
    private String message;

    private List<HistoryMessage> history = new ArrayList<>();

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

    public List<HistoryMessage> getHistory() {
        return history;
    }

    public void setHistory(List<HistoryMessage> history) {
        this.history = history != null ? history : new ArrayList<>();
    }
}

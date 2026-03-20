package com.learnplatform.dto.workflow;

/**
 * LLM 对话历史消息
 * role: "system" | "user" | "assistant"
 */
public class HistoryMessage {

    private String role;
    private String content;
    /** 关联的步骤 ID（可选，用于过滤） */
    private String stepId;
    /** 是否来自用户上传文件 */
    private Boolean isFileHistory;

    public HistoryMessage() {}

    public HistoryMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public HistoryMessage(String role, String content, String stepId) {
        this.role = role;
        this.content = content;
        this.stepId = stepId;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStepId() { return stepId; }
    public void setStepId(String stepId) { this.stepId = stepId; }

    public Boolean getIsFileHistory() { return isFileHistory != null && isFileHistory; }
    public void setIsFileHistory(Boolean isFileHistory) { this.isFileHistory = isFileHistory; }
}

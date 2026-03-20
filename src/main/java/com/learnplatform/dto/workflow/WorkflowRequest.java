package com.learnplatform.dto.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 工作流请求 DTO
 * 对应原 Node.js POST body
 */
public class WorkflowRequest {

    /** 工作流步骤列表（有序） */
    @JsonProperty("rawMsgList")
    private List<WorkflowStep> rawMsgList;

    /** 用户上传的文件内容或历史记录（作为初始 file history） */
    @JsonProperty("chatHistoryList")
    private List<HistoryMessage> chatHistoryList;

    /** 唯一标识，用于中断流式请求 */
    @JsonProperty("streamingId")
    private String streamingId;

    /** 是否最终生成 Word 文件 */
    @JsonProperty("generateFile")
    private Boolean generateFile = false;

    /** 工作流名称 / 报告名称 */
    @JsonProperty("workflowName")
    private String workflowName;

    /**
     * 返回模式:
     * "api"  → HTTP Chunked SSE（默认）
     * "socket" → WebSocket（目前 MVP 不支持）
     */
    @JsonProperty("returnMode")
    private String returnMode = "api";

    /** 预定义变量 key-value（用于 {{VAR}} 替换） */
    @JsonProperty("predefinedVariables")
    private java.util.Map<String, String> predefinedVariables;

    // ===== Getters & Setters =====

    public List<WorkflowStep> getRawMsgList() { return rawMsgList; }
    public void setRawMsgList(List<WorkflowStep> rawMsgList) { this.rawMsgList = rawMsgList; }

    public List<HistoryMessage> getChatHistoryList() { return chatHistoryList; }
    public void setChatHistoryList(List<HistoryMessage> chatHistoryList) { this.chatHistoryList = chatHistoryList; }

    public String getStreamingId() { return streamingId; }
    public void setStreamingId(String streamingId) { this.streamingId = streamingId; }

    public Boolean getGenerateFile() { return generateFile != null && generateFile; }
    public void setGenerateFile(Boolean generateFile) { this.generateFile = generateFile; }

    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }

    public String getReturnMode() { return returnMode != null ? returnMode : "api"; }
    public void setReturnMode(String returnMode) { this.returnMode = returnMode; }

    public java.util.Map<String, String> getPredefinedVariables() { return predefinedVariables; }
    public void setPredefinedVariables(java.util.Map<String, String> predefinedVariables) { this.predefinedVariables = predefinedVariables; }
}

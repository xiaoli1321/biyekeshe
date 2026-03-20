package com.learnplatform.dto.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 工作流步骤 DTO
 * 对应原 Node.js 中的 raw_msg_obj / WorkflowStep
 */
public class WorkflowStep {

    /** 步骤唯一 ID */
    private String id;

    /**
     * 步骤类型枚举:
     * text / fixed / table / pic / pic_code_tool / image_generation / rag / api / classifier / ai_excel
     */
    private String type;

    /** 发给大模型的 prompt 内容（fixed type 时为直接输出内容） */
    private String content;

    /** 可选，步骤标题（MD格式），会直接输出在内容前面 */
    private String title;

    /** 使用的模型名，如 deepseek_v3 / glm4 / qwen 等 */
    private String model;

    /** 可选，系统提示词（覆盖默认 system prompt） */
    @JsonProperty("system_prompt")
    private String systemPrompt;

    /**
     * 历史上下文模式:
     * all / file_history_only / processed_only / none
     */
    @JsonProperty("history_mode")
    private String historyMode = "all";

    /**
     * 文件内容模式:
     * all / none
     */
    @JsonProperty("file_mode")
    private String fileMode = "all";

    /** 是否为「思考过程」步骤，true 时不计入最终 Word 文档 */
    @JsonProperty("is_thinking_process")
    private Boolean isThinkingProcess = false;

    /** type=pic 时指定图类型: mindMap / flowchart / sequenceDiagram */
    @JsonProperty("pic_type")
    private String picType;

    /** 图表主题色 */
    @JsonProperty("theme_color")
    private String themeColor;

    /** 是否开启模型思考模式（如 deepseek-r1） */
    @JsonProperty("enable_thinking")
    private Boolean enableThinking = false;

    /** type=rag 时：向量库 ID 列表 */
    private java.util.List<String> datasets;

    /** RAG 检索数量 */
    @JsonProperty("embedding_limit")
    private Integer embeddingLimit = 5;

    // ===== Getters & Setters =====

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type != null ? type : "text"; }
    public void setType(String type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getModel() { return model != null ? model : "deepseek_v3"; }
    public void setModel(String model) { this.model = model; }

    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }

    public String getHistoryMode() { return historyMode != null ? historyMode : "all"; }
    public void setHistoryMode(String historyMode) { this.historyMode = historyMode; }

    public String getFileMode() { return fileMode; }
    public void setFileMode(String fileMode) { this.fileMode = fileMode; }

    public Boolean getIsThinkingProcess() { return isThinkingProcess != null && isThinkingProcess; }
    public void setIsThinkingProcess(Boolean isThinkingProcess) { this.isThinkingProcess = isThinkingProcess; }

    public String getPicType() { return picType; }
    public void setPicType(String picType) { this.picType = picType; }

    public String getThemeColor() { return themeColor; }
    public void setThemeColor(String themeColor) { this.themeColor = themeColor; }

    public Boolean getEnableThinking() { return enableThinking != null && enableThinking; }
    public void setEnableThinking(Boolean enableThinking) { this.enableThinking = enableThinking; }

    public java.util.List<String> getDatasets() { return datasets; }
    public void setDatasets(java.util.List<String> datasets) { this.datasets = datasets; }

    public Integer getEmbeddingLimit() { return embeddingLimit != null ? embeddingLimit : 5; }
    public void setEmbeddingLimit(Integer embeddingLimit) { this.embeddingLimit = embeddingLimit; }
}

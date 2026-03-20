package com.learnplatform.dto.workflow;

/**
 * 步骤结果汇总链表节点
 * 对应原 JS 中的 md_result_list 元素
 */
public class MdResultNode {

    /** 该步骤 Markdown 内容 */
    private String content;

    /** 节点类型: text / pic / title / fixed */
    private String type;

    /** type=pic 时有值: mindMap / flowchart / sequenceDiagram */
    private String picType;

    /** true 时不计入最终 Word 文档（思考过程步骤） */
    private Boolean isThinkingProcess;

    /** 关联步骤 ID */
    private String stepId;

    /** type=title 时：标题层级 */
    private Integer titleLevel;

    public MdResultNode() {}

    public MdResultNode(String content, String type, String stepId) {
        this.content = content;
        this.type = type;
        this.stepId = stepId;
        this.isThinkingProcess = false;
    }

    // ===== Getters & Setters =====

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPicType() { return picType; }
    public void setPicType(String picType) { this.picType = picType; }

    public Boolean getIsThinkingProcess() { return isThinkingProcess != null && isThinkingProcess; }
    public void setIsThinkingProcess(Boolean isThinkingProcess) { this.isThinkingProcess = isThinkingProcess; }

    public String getStepId() { return stepId; }
    public void setStepId(String stepId) { this.stepId = stepId; }

    public Integer getTitleLevel() { return titleLevel; }
    public void setTitleLevel(Integer titleLevel) { this.titleLevel = titleLevel; }
}

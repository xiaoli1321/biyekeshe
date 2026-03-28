package com.learnplatform.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * AI Agent 实体 (AgentX Architecture)
 */
@Document(collection = "agents")
public class Agent {
    @Id
    private String id;
    private String name;
    private String avatar; // 新增：头像
    private String description;
    private String systemPrompt;
    private String welcomeMessage; // 新增：欢迎语
    private boolean enabled = true; // 新增：启用状态
    private String modelId; // 关联 LlmProvider
    private String kbCollectionId; // 关联知识库 (RAG)
    private String userId;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastModifiedAt = LocalDateTime.now(); // 新增：修改时间

    public Agent() {}

    public Agent(String name, String systemPrompt, String modelId, String userId) {
        this.name = name;
        this.systemPrompt = systemPrompt;
        this.modelId = modelId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
    public String getWelcomeMessage() { return welcomeMessage; }
    public void setWelcomeMessage(String welcomeMessage) { this.welcomeMessage = welcomeMessage; }
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getKbCollectionId() { return kbCollectionId; }
    public void setKbCollectionId(String kbCollectionId) { this.kbCollectionId = kbCollectionId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
}

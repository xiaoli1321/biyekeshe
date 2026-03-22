package com.learnplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * LLM API 配置类
 * 读取 application.properties 中 llm.* 配置项
 */
@Configuration
@ConfigurationProperties(prefix = "llm")
public class LlmConfig {

    /** Deepseek API 配置 */
    private Deepseek deepseek = new Deepseek();

    /** 向量嵌入 API 配置 (默认为 OpenAI 兼容格式) */
    private Embedding embedding = new Embedding();

    public static class Deepseek {
        private String apiKey = "";
        private String baseUrl = "https://api.deepseek.com/v1";
        private String model = "deepseek-chat";
        private Integer maxTokens = 4096;
        private Double temperature = 0.7;
        private Integer timeoutSeconds = 120;

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public Integer getMaxTokens() { return maxTokens; }
        public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }

        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }

        public Integer getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    }

    public static class Embedding {
        private String apiKey = "";
        private String baseUrl = "https://api.openai.com/v1"; // 默认，通常用户会改为 Ollama 或 OpenAI
        private String model = "text-embedding-3-small";
        private Integer timeoutSeconds = 60;

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public Integer getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    }

    public Deepseek getDeepseek() { return deepseek; }
    public void setDeepseek(Deepseek deepseek) { this.deepseek = deepseek; }
    public Embedding getEmbedding() { return embedding; }
    public void setEmbedding(Embedding embedding) { this.embedding = embedding; }
}

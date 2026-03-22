package com.learnplatform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnplatform.config.LlmConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 向量嵌入服务
 * 调用 OpenAI 兼容的 /embeddings 接口
 */
@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    @Autowired
    private LlmConfig llmConfig;

    @Autowired
    private ObjectMapper objectMapper;

    private WebClient webClient;

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = WebClient.builder()
                    .baseUrl(llmConfig.getEmbedding().getBaseUrl())
                    .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                    .build();
        }
        return webClient;
    }

    /**
     * 获取文本的向量表示 (同步调用)
     */
    public float[] getEmbedding(String text) {
        if (text == null || text.isBlank()) {
            return new float[0];
        }

        LlmConfig.Embedding config = llmConfig.getEmbedding();
        
        Map<String, Object> body = new HashMap<>();
        body.put("model", config.getModel());
        body.put("input", text);

        try {
            Mono<String> responseMono = getWebClient().post()
                    .uri("/embeddings")
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(config.getTimeoutSeconds()));

            String responseJson = responseMono.block();
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode embeddingNode = root.path("data").get(0).path("embedding");

            if (embeddingNode.isArray()) {
                float[] vector = new float[embeddingNode.size()];
                for (int i = 0; i < embeddingNode.size(); i++) {
                    vector[i] = (float) embeddingNode.get(i).asDouble();
                }
                return vector;
            }
        } catch (Exception e) {
            log.error("Failed to get embedding: {}", e.getMessage());
            // 如果失败，返回一个 Mock 向量以保证流程不中断，或者抛出异常
            // 这里我们返回 null，让调用方决定如何处理 (通常 KnowledgeBaseService 应该处理错误)
            return null;
        }

        return null;
    }
}

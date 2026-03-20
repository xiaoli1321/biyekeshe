package com.learnplatform.service.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnplatform.config.LlmConfig;
import com.learnplatform.dto.workflow.HistoryMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Flux;

import java.io.PrintWriter;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * LLM 流式调用核心服务
 * 等价于原 Node.js 的 streaming_api_wrapper.js + streaming_wrapper()
 * 支持 Deepseek-V3 (OpenAI 兼容接口)，通过 SSE 实时流式输出到 PrintWriter
 */
@Service
public class LlmStreamingService {

    private static final Logger log = LoggerFactory.getLogger(LlmStreamingService.class);

    @Autowired
    private LlmConfig llmConfig;

    @Autowired
    private ObjectMapper objectMapper;

    /** WebClient 实例（惰性初始化，根据 baseUrl 创建） */
    private WebClient webClient;

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = WebClient.builder()
                    .baseUrl(llmConfig.getDeepseek().getBaseUrl())
                    .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                    .build();
        }
        return webClient;
    }

    /**
     * 流式调用大模型，将 token 实时写入 writer
     * 对应原 JS streaming_wrapper()
     *
     * @param prompt      用户 prompt
     * @param model       模型名（如 deepseek_v3）
     * @param history     历史消息列表
     * @param systemPrompt 系统提示词（可为 null）
     * @param writer      HTTP 响应的 PrintWriter（流式写出）
     * @param abortFlag   中止标志，true 时停止循环
     * @return 完整生成的文本内容（用于追加到 processedHistoryList）
     */
    public String streamingWrapper(
            String prompt,
            String model,
            List<HistoryMessage> history,
            String systemPrompt,
            PrintWriter writer,
            AtomicBoolean abortFlag
    ) {
        // 组装消息列表
        List<Map<String, String>> messages = buildMessages(history, systemPrompt, prompt);

        // 构造请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", resolveModelName(model));
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", llmConfig.getDeepseek().getMaxTokens());
        requestBody.put("temperature", llmConfig.getDeepseek().getTemperature());
        requestBody.put("stream", true);

        StringBuilder fullResponse = new StringBuilder();

        try {
            String apiKey = llmConfig.getDeepseek().getApiKey();
            if (apiKey == null || apiKey.isBlank()) {
                // API key 未配置时输出提示（方便开发调试）
                String mock = "[LLM未配置API Key，这是模拟响应] 模型: " + model + "，Prompt: " + prompt.substring(0, Math.min(50, prompt.length())) + "...";
                writer.write(mock);
                writer.flush();
                return mock;
            }

            // 发起 SSE 流式请求
            ParameterizedTypeReference<ServerSentEvent<String>> type =
                    new ParameterizedTypeReference<>() {};

            Flux<ServerSentEvent<String>> eventStream = getWebClient().post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToFlux(type)
                    .timeout(Duration.ofSeconds(llmConfig.getDeepseek().getTimeoutSeconds()));

            // 同步消费 SSE 流
            eventStream.toStream().forEach(event -> {
                if (abortFlag != null && abortFlag.get()) {
                    return; // 已中止，忽略后续 token
                }

                String data = event.data();
                if (data == null || data.isBlank() || "[DONE]".equals(data)) {
                    return;
                }

                try {
                    JsonNode node = objectMapper.readTree(data);
                    JsonNode choices = node.get("choices");
                    if (choices != null && choices.isArray() && choices.size() > 0) {
                        JsonNode delta = choices.get(0).get("delta");
                        if (delta != null && delta.has("content")) {
                            String token = delta.get("content").asText();
                            if (token != null && !token.isEmpty()) {
                                fullResponse.append(token);
                                sendTokenEvent(writer, token);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.debug("Parse SSE token error (skipped): {}", data);
                }
            });

        } catch (Exception e) {
            log.error("LLM streaming error for model {}: {}", model, e.getMessage());
            String errorMsg = "\n[LLM调用错误: " + e.getMessage() + "]\n";
            writer.write(errorMsg);
            writer.flush();
            return errorMsg;
        }

        return fullResponse.toString();
    }

    /**
     * 构建发给 LLM 的 messages 数组（含 system prompt + 历史 + 当前 prompt）
     */
    private List<Map<String, String>> buildMessages(
            List<HistoryMessage> history,
            String systemPrompt,
            String prompt
    ) {
        List<Map<String, String>> messages = new ArrayList<>();

        // 1. system prompt（如果有）
        String effectiveSystem = systemPrompt;
        if (effectiveSystem == null || effectiveSystem.isBlank()) {
            effectiveSystem = "你是一个专业的内容生成助手，帮助用户生成高质量的报告和文档。请使用 Markdown 格式输出内容。";
        }
        messages.add(Map.of("role", "system", "content", effectiveSystem));

        // 2. 历史消息（过滤掉 stepId 字段，只留 role + content）
        if (history != null) {
            for (HistoryMessage msg : history) {
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            }
        }

        // 3. 当前 prompt
        messages.add(Map.of("role", "user", "content", prompt));

        return messages;
    }

    /**
     * 将前端传入的 model 名映射到实际 API 模型名
     * 等价于原 JS streaming_api_wrapper 的 model routing
     */
    private String resolveModelName(String modelAlias) {
        if (modelAlias == null) {
            return llmConfig.getDeepseek().getModel();
        }
        return switch (modelAlias.toLowerCase()) {
            case "deepseek_v3", "deepseek-v3", "deepseek-chat" -> "deepseek-chat";
            case "deepseek_r1", "deepseek-r1", "deepseek-reasoner" -> "deepseek-reasoner";
            case "glm4", "glm-4" -> "glm-4"; // 后续可扩展为调 GLM API
            default -> llmConfig.getDeepseek().getModel(); // fallback 到配置的默认模型
        };
    }

    /**
     * 以 SSE 格式发送 token
     * data: {"type":"token","content":"..."}\n\n
     */
    private void sendTokenEvent(PrintWriter writer, String token) {
        try {
            Map<String, String> event = Map.of("type", "token", "content", token);
            writer.write("data: " + objectMapper.writeValueAsString(event) + "\n\n");
            writer.flush(); // 确保立刻发送到网络
        } catch (Exception e) {
            log.warn("Failed to send token event: {}", e.getMessage());
        }
    }
}

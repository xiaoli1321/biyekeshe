package com.learnplatform.service;

import com.learnplatform.entity.Agent;
import com.learnplatform.entity.Conversation;
import com.learnplatform.entity.Message;
import com.learnplatform.repository.AgentRepository;
import com.learnplatform.repository.ConversationRepository;
import com.learnplatform.repository.MessageRepository;
import com.learnplatform.service.workflow.LlmStreamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Agent 业务服务 (AgentX Architecture)
 * 负责对话逻辑、上下文管理 (The N+1 Strategy) 和 SSE 打字机效果
 */
@Service
public class AgentService {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private LlmStreamingService llmStreamingService;

    /**
     * 实现 "The N+1 Strategy" (摘要 + 滑动窗口) 的上下文构建
     * 当前简化版：提取最近的 K 条历史记录
     */
    public List<Message> buildContext(String conversationId, int windowSize) {
        List<Message> history = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        if (history.size() <= windowSize) {
            return history;
        }
        // 滑动窗口：取最近的 windowSize 条
        return history.subList(history.size() - windowSize, history.size());
    }

    /**
     * 核心对话逻辑：流式响应并持久化
     */
    public Flux<String> chatStream(String agentId, String conversationId, String userContent) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // 1. 保存用户消息
        Message userMsg = new Message(conversationId, "user", userContent);
        messageRepository.save(userMsg);

        // 2. 更新会话最后活跃时间
        conv.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conv);

        // 3. 构建上下文 (N+1 策略)
        List<Message> context = buildContext(conversationId, 10);
        
        // 4. 调用 LLM 流式服务
        // 构造 System Prompt 和历史记录 (转换格式)
        List<Map<String, String>> history = context.stream()
                .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                .collect(Collectors.toList());
        
        StringBuilder assistantReply = new StringBuilder();

        return llmStreamingService.getGeneralFlux(userContent, agent.getModelId(), history, agent.getSystemPrompt())
                .doOnNext(token -> {
                    // 累积回复内容，用于最后持久化
                    assistantReply.append(token);
                })
                .doOnComplete(() -> {
                    // 5. 对话完成后保存助手消息
                    Message assistantMsg = new Message(conversationId, "assistant", assistantReply.toString());
                    messageRepository.save(assistantMsg);
                });
    }

    // Agent CRUD logic
    public Agent createAgent(Agent agent) {
        if (agent.getName() == null || agent.getName().trim().isEmpty()) {
            throw new RuntimeException("Agent name cannot be empty");
        }
        agent.setCreatedAt(LocalDateTime.now());
        agent.setLastModifiedAt(LocalDateTime.now());
        return agentRepository.save(agent);
    }

    public List<Agent> getPublicAgents() {
        return agentRepository.findByEnabledTrue();
    }

    public List<Agent> getAgentsByUser(String userId) {
        return agentRepository.findByUserId(userId);
    }

    public Conversation startConversation(String agentId, String userId, String title) {
        Conversation conv = new Conversation(agentId, userId, title);
        return conversationRepository.save(conv);
    }

    public Agent updateAgent(String agentId, String userId, Agent details) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        if (!agent.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        agent.setName(details.getName());
        agent.setAvatar(details.getAvatar());
        agent.setDescription(details.getDescription());
        agent.setSystemPrompt(details.getSystemPrompt());
        agent.setWelcomeMessage(details.getWelcomeMessage());
        agent.setEnabled(details.isEnabled());
        agent.setModelId(details.getModelId());
        agent.setLastModifiedAt(LocalDateTime.now());
        return agentRepository.save(agent);
    }

    public void deleteAgent(String agentId, String userId) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        if (!agent.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        agentRepository.delete(agent);
    }
}

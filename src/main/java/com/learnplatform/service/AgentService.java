package com.learnplatform.service;

import com.learnplatform.entity.Agent;
import com.learnplatform.entity.Conversation;
import com.learnplatform.entity.KbChunk;
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

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    /**
     * 实现 "The N+1 Strategy" (摘要 + 滑动窗口) 的上下文构建
     * 当前简化版：提取最近的 K 条历史记录
     */
    public List<Message> buildContext(String conversationId, int windowSize) {
        List<Message> history = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        if (history.size() <= windowSize) {
            return history;
        }
        // "The N+1 Strategy" Sliding Window (currently simplified as last N)
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
        
        // 4. RAG Logic (Knowledge Base Integration)
        String llmInput = userContent;
        System.out.println("DEBUG: Agent [" + agent.getName() + " (ID: " + agentId + ")] kbCollectionId: [" + agent.getKbCollectionId() + "]");
        
        if (agent.getKbCollectionId() != null && !agent.getKbCollectionId().isEmpty()) {
            System.out.println("RAG: Searching Knowledge Base collection: " + agent.getKbCollectionId() + " for query: " + userContent);
            List<KbChunk> chunks = knowledgeBaseService.hybridSearch(userContent, agent.getKbCollectionId(), 5);
            if (!chunks.isEmpty()) {
                System.out.println("RAG: Success. Found " + chunks.size() + " chunks.");
                String contextStr = chunks.stream()
                        .map(KbChunk::getContent)
                        .collect(Collectors.joining("\n---\n"));
                
                // Enhance prompt for better RAG compliance (Instructional Authority)
                llmInput = "### [系统：检索到相关背景知识]\n" + 
                           contextStr + "\n\n" +
                           "### [用户问题]\n" + 
                           userContent + "\n\n" +
                           "--- \n" +
                           "【指令】请严格基于上述[背景知识]回答用户问题。若知识中没有相关内容，请按照你的通用知识实话实说，但必须优先核对背景知识。";
            } else {
                System.out.println("RAG: No relevant chunks found in Knowledge Base collection: " + agent.getKbCollectionId());
            }
        } else {
            System.out.println("RAG: KB Collection ID is empty/null, skipping RAG.");
        }

        // 5. 构造历史记录 (转换格式)
        List<Map<String, String>> history = context.stream()
                .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                .collect(Collectors.toList());

        // 6. 调用 LLM 流式服务
        StringBuilder assistantReply = new StringBuilder();

        return llmStreamingService.getGeneralFlux(llmInput, agent.getModelId(), history, agent.getSystemPrompt())
                .doOnNext(token -> {
                    assistantReply.append(token);
                })
                .doOnComplete(() -> {
                    // Persistence: Assistant Reply
                    if (assistantReply.length() > 0) {
                        Message assistantMsg = new Message(conversationId, "assistant", assistantReply.toString());
                        messageRepository.save(assistantMsg);
                    }
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
        conv.setCreatedAt(LocalDateTime.now());
        conv.setLastMessageAt(LocalDateTime.now());
        return conversationRepository.save(conv);
    }

    public List<Conversation> getConversationsByAgent(String agentId, String userId) {
        return conversationRepository.findByAgentIdAndUserIdOrderByLastMessageAtDesc(agentId, userId);
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
        agent.setKbCollectionId(details.getKbCollectionId());
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

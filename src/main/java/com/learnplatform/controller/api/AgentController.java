package com.learnplatform.controller.api;

import com.learnplatform.entity.Agent;
import com.learnplatform.entity.Conversation;
import com.learnplatform.entity.Message;
import com.learnplatform.repository.MessageRepository;
import com.learnplatform.security.UserPrincipal;
import com.learnplatform.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * Agent 控制器 (AgentX Architecture)
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/agents")
public class AgentController {

    @Autowired
    private AgentService agentService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<List<Agent>> getPublicAgents() {
        return ResponseEntity.ok(agentService.getPublicAgents());
    }

    @GetMapping("/my")
    public ResponseEntity<List<Agent>> getMyAgents(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(agentService.getAgentsByUser(userPrincipal.getId()));
    }

    @PostMapping
    public ResponseEntity<Agent> createAgent(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody Agent agent) {
        agent.setUserId(userPrincipal.getId());
        return ResponseEntity.ok(agentService.createAgent(agent));
    }

    @PostMapping("/{agentId}/conversations")
    public ResponseEntity<Conversation> startConversation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("agentId") String agentId,
            @RequestBody Map<String, String> body) {
        String title = body.getOrDefault("title", "新对话");
        return ResponseEntity.ok(agentService.startConversation(agentId, userPrincipal.getId(), title));
    }

    @GetMapping("/{agentId}/conversations")
    public ResponseEntity<List<Conversation>> getConversationsByAgent(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("agentId") String agentId) {
        return ResponseEntity.ok(agentService.getConversationsByAgent(agentId, userPrincipal.getId()));
    }

    @GetMapping(value = "/{agentId}/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(
            @PathVariable("agentId") String agentId,
            @RequestParam("conversationId") String conversationId,
            @RequestParam("message") String message) {
        return agentService.chatStream(agentId, conversationId, message)
                .map(token -> {
                    try {
                        Map<String, String> event = Map.of("type", "token", "content", token);
                        return objectMapper.writeValueAsString(event);
                    } catch (Exception e) {
                        return "{\"type\":\"error\",\"content\":\"Serialization error\"}";
                    }
                });
    }
    
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable("conversationId") String conversationId) {
        return ResponseEntity.ok(messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId));
    }

    @PutMapping("/{agentId}")
    public ResponseEntity<Agent> updateAgent(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("agentId") String agentId, @RequestBody Agent agentDetails) {
        return ResponseEntity.ok(agentService.updateAgent(agentId, userPrincipal.getId(), agentDetails));
    }

    @DeleteMapping("/{agentId}")
    public ResponseEntity<Void> deleteAgent(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("agentId") String agentId) {
        agentService.deleteAgent(agentId, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
}

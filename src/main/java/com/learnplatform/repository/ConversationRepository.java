package com.learnplatform.repository;

import com.learnplatform.entity.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ConversationRepository extends MongoRepository<Conversation, String> {
    List<Conversation> findByUserId(String userId);
    List<Conversation> findByUserIdOrderByLastMessageAtDesc(String userId);
    List<Conversation> findByAgentIdAndUserIdOrderByLastMessageAtDesc(String agentId, String userId);
}

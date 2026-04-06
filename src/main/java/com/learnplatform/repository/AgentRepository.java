package com.learnplatform.repository;

import com.learnplatform.entity.Agent;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AgentRepository extends MongoRepository<Agent, String> {
    List<Agent> findByUserId(String userId);
    List<Agent> findByEnabledTrue();
    java.util.Optional<Agent> findByName(String name);
}

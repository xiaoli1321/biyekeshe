package com.learnplatform.repository;

import com.learnplatform.entity.LlmProvider;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LlmProviderRepository extends MongoRepository<LlmProvider, String> {
    List<LlmProvider> findByUserId(String userId);
}

package com.learnplatform.repository;

import com.learnplatform.entity.KbCollection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KbCollectionRepository extends MongoRepository<KbCollection, String> {
    List<KbCollection> findByUserId(String userId);
}

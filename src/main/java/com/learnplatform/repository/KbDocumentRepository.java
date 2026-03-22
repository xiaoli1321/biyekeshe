package com.learnplatform.repository;

import com.learnplatform.entity.KbDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KbDocumentRepository extends MongoRepository<KbDocument, String> {
    List<KbDocument> findByCollectionId(String collectionId);
}

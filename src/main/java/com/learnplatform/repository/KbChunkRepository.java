package com.learnplatform.repository;

import com.learnplatform.entity.KbChunk;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KbChunkRepository extends MongoRepository<KbChunk, String> {
    List<KbChunk> findByDocumentId(String documentId);
    
    // Using MongoDB Text Search $text index
    @Query("{ $text: { $search: ?0 } }")
    List<KbChunk> searchByKeyword(String query);

    // Fallback search using regex if text index is missing or query is simple
    List<KbChunk> findByContentContainingIgnoreCase(String content);
}

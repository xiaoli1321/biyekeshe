package com.learnplatform.service;

import com.learnplatform.entity.KbChunk;
import com.learnplatform.repository.AgentRepository;
import com.learnplatform.repository.KbChunkRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class RagConnectivityTest {

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private KbChunkRepository chunkRepository;

    @Test
    public void testEmbeddingRecall() {
        String testQuery = "你好";
        System.out.println("Testing Embedding API for query: " + testQuery);
        float[] vector = embeddingService.getEmbedding(testQuery);
        assertNotNull(vector, "Embedding vector should not be null");
        System.out.println("SUCCESS: Received vector of length " + vector.length);
    }

    @Test
    public void testDbRecords() {
        System.out.println("--- Database Record Check ---");
        long agentCount = agentRepository.count();
        System.out.println("Total Agents: " + agentCount);
        
        agentRepository.findAll().forEach(agent -> {
            System.out.println("Agent: " + agent.getName() + 
                               ", ID: " + agent.getId() + 
                               ", kbCollectionId: " + agent.getKbCollectionId());
        });

        long chunkCount = chunkRepository.count();
        System.out.println("Total KbChunks: " + chunkCount);
        
        if (chunkCount > 0) {
            KbChunk firstChunk = chunkRepository.findAll().get(0);
            System.out.println("First chunk vector status: " + firstChunk.getVectorStatus());
            System.out.println("First chunk embedding length: " + 
                               (firstChunk.getEmbedding() != null ? firstChunk.getEmbedding().length : 0));
        }
    }
}

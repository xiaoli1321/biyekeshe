package com.learnplatform.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "kb_chunks")
public class KbChunk {
    @Id
    private String id;
    private String documentId;
    
    @TextIndexed
    private String content; // Text for keyword search
    
    private float[] embedding; // Vector for semantic search
    
    private String vectorStatus; // PENDING, DONE, ERROR
    
    private int chunkIndex; // Sequence number

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
    public String getVectorStatus() { return vectorStatus; }
    public void setVectorStatus(String vectorStatus) { this.vectorStatus = vectorStatus; }
    public int getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(int chunkIndex) { this.chunkIndex = chunkIndex; }
}

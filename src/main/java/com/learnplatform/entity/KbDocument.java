package com.learnplatform.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "kb_documents")
public class KbDocument {
    @Id
    private String id;
    private String filename;
    private String fileType; // pdf, docx, txt
    private String collectionId; // Knowledge Base Collection ID
    private LocalDateTime uploadDate;
    private String status; // PARSING, READY, ERROR
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public String getCollectionId() { return collectionId; }
    public void setCollectionId(String collectionId) { this.collectionId = collectionId; }
    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

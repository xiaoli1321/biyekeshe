package com.learnplatform.service;

import com.learnplatform.entity.KbChunk;
import com.learnplatform.entity.KbDocument;
import com.learnplatform.repository.KbChunkRepository;
import com.learnplatform.repository.KbDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgeBaseService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseService.class);

    @Autowired
    private KbDocumentRepository documentRepository;

    @Autowired
    private KbChunkRepository chunkRepository;

    @Autowired
    private DocumentParserService parserService;

    @Autowired
    private EmbeddingService embeddingService;

    /**
     * 处理上传文档并持久化
     */
    public KbDocument processUpload(MultipartFile file, String collectionId) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) filename = "unknown";
        String fileType = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

        log.info("Processing upload: {} for collection: {}", filename, collectionId);

        // 1. 创建文档元数据
        KbDocument doc = new KbDocument();
        doc.setFilename(filename);
        doc.setFileType(fileType);
        doc.setCollectionId(collectionId);
        doc.setUploadDate(LocalDateTime.now());
        doc.setStatus("PARSING");
        doc = documentRepository.save(doc);

        try {
            // 2. 解析文本
            String fullText = parserService.extractText(file);
            log.info("Extracted text length: {}", fullText.length());

            // 3. 分块
            List<String> textChunks = parserService.chunkText(fullText);
            log.info("Split into {} chunks", textChunks.size());

            // 4. 对每个分块进行向量化并保存
            List<KbChunk> chunksToSave = new ArrayList<>();
            for (int i = 0; i < textChunks.size(); i++) {
                String content = textChunks.get(i);
                KbChunk chunk = new KbChunk();
                chunk.setDocumentId(doc.getId());
                chunk.setContent(content);
                chunk.setChunkIndex(i);
                
                // 调用真实 Embedding 服务
                float[] vector = embeddingService.getEmbedding(content);
                if (vector == null) {
                    log.error("Embedding generation failed for chunk {} of doc {}", i, doc.getId());
                    chunk.setEmbedding(new float[0]);
                    chunk.setVectorStatus("ERROR");
                } else {
                    chunk.setEmbedding(vector);
                    chunk.setVectorStatus("DONE");
                }
                chunksToSave.add(chunk);
            }
            chunkRepository.saveAll(chunksToSave);
            log.info("Saved {} chunks to repository", chunksToSave.size());

            // 5. 更新文档状态
            doc.setStatus("READY");
        } catch (Exception e) {
            log.error("Failed to process upload: {}", e.getMessage(), e);
            doc.setStatus("ERROR");
            throw e;
        } finally {
            documentRepository.save(doc);
        }

        return doc;
    }

    /**
     * 手动录入文本
     */
    public KbDocument processTextEntry(String title, String content, String collectionId) {
        log.info("Processing text entry: {} for collection: {}", title, collectionId);
        KbDocument doc = new KbDocument();
        doc.setFilename(title);
        doc.setFileType("txt");
        doc.setCollectionId(collectionId);
        doc.setUploadDate(LocalDateTime.now());
        doc.setStatus("PARSING");
        doc = documentRepository.save(doc);

        List<String> textChunks = parserService.chunkText(content);
        List<KbChunk> chunksToSave = new ArrayList<>();
        for (int i = 0; i < textChunks.size(); i++) {
            String c = textChunks.get(i);
            KbChunk chunk = new KbChunk();
            chunk.setDocumentId(doc.getId());
            chunk.setContent(c);
            chunk.setChunkIndex(i);
            
            float[] vector = embeddingService.getEmbedding(c);
            if (vector != null) {
                chunk.setEmbedding(vector);
                chunk.setVectorStatus("DONE");
            } else {
                chunk.setEmbedding(new float[0]);
                chunk.setVectorStatus("ERROR");
            }
            chunksToSave.add(chunk);
        }
        chunkRepository.saveAll(chunksToSave);

        doc.setStatus("READY");
        return documentRepository.save(doc);
    }

    public List<KbDocument> getDocumentsByCollection(String collectionId) {
        return documentRepository.findByCollectionId(collectionId);
    }

    public void deleteDocument(String id) {
        List<KbChunk> chunks = chunkRepository.findByDocumentId(id);
        chunkRepository.deleteAll(chunks);
        documentRepository.deleteById(id);
    }

    public List<KbChunk> getChunksByDocument(String documentId) {
        return chunkRepository.findByDocumentId(documentId);
    }

    /**
     * 混合检索 logic (含召回、重排序融合)
     * 流程：关键词召回 + 向量召回 -> 重排序 (Rerank) -> 返回 TopK
     */
    public List<KbChunk> hybridSearch(String query, String collectionId, int topK) {
        if (query == null || query.isBlank()) return Collections.emptyList();

        log.info("Hybrid search for query [{}] in collection [{}]", query, collectionId);

        // 1. 获取该 Collection 下的所有文档 ID
        List<String> docIds = documentRepository.findByCollectionId(collectionId).stream()
                .map(KbDocument::getId)
                .toList();
        
        if (docIds.isEmpty()) {
            log.warn("Collection [{}] has no documents - check if files were uploaded and parsed successfully", collectionId);
            return Collections.emptyList();
        }
        log.info("RAG Trace: Searching across {} documents.", docIds.size());

        // --- 召回阶段 1: 关键词召白 ---
        Set<String> keywordChunkIds = new HashSet<>();
        List<KbChunk> keywordRecall = new ArrayList<>();
        try {
            // Attempt standard MongoDB Text search first
            keywordRecall = chunkRepository.searchByKeyword(query).stream()
                    .filter(c -> docIds.contains(c.getDocumentId()))
                    .collect(Collectors.toList());
            log.info("RAG Trace: Keyword recall found {} chunks", keywordRecall.size());
        } catch (Exception e) {
            log.warn("Text search failed or not configured: {}", e.getMessage());
        }

        // --- 召回阶段 1.5: 分词增强召回 (Robust Fallback) ---
        if (keywordRecall.isEmpty()) {
            List<String> keywords = tokenizeQuery(query);
            log.info("RAG Trace: No text match, starting fallback for keywords: {}", keywords);
            // Search chunks containing any keyword
            Map<String, Integer> hitCount = new HashMap<>();
            Map<String, KbChunk> hitMap = new HashMap<>();

            for (String kw : keywords) {
                List<KbChunk> matches = chunkRepository.findByContentContainingIgnoreCase(kw).stream()
                        .filter(c -> docIds.contains(c.getDocumentId()))
                        .toList();
                for (KbChunk m : matches) {
                    hitCount.put(m.getId(), hitCount.getOrDefault(m.getId(), 0) + 1);
                    hitMap.put(m.getId(), m);
                }
            }
            
            // Sort by hit count (descending)
            keywordRecall = hitCount.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .limit(topK * 2)
                    .map(e -> hitMap.get(e.getKey()))
                    .toList();
            log.info("RAG Trace: Hybrid keyword fallback found {} chunks", keywordRecall.size());
        }
        for (KbChunk c : keywordRecall) keywordChunkIds.add(c.getId());

        // --- 召回阶段 2: 向量召回 ---
        log.info("RAG Trace: Requesting query embedding...");
        float[] queryVector = embeddingService.getEmbedding(query);
        if (queryVector == null) {
            log.error("RAG ERROR: Vectorization failed for query [{}]. Check Embedding API connectivity and key.", query);
        }
        
        List<KbChunk> vectorRecall = new ArrayList<>();
        if (queryVector != null) {
            // 获取该 Collection 下的所有片段进行遍历计算 (本地 MVP 方案)
            List<KbChunk> allCollectionChunks = new ArrayList<>();
            for (String docId : docIds) {
                allCollectionChunks.addAll(chunkRepository.findByDocumentId(docId));
            }
            log.info("RAG Trace: Comparing query against {} total chunks in collection", allCollectionChunks.size());

            vectorRecall = allCollectionChunks.stream()
                    .filter(c -> c.getEmbedding() != null && c.getEmbedding().length > 0)
                    .peek(c -> {
                        if (c.getEmbedding().length != queryVector.length) {
                            log.error("RAG DIMENSION MISMATCH: Chunk {} has dim {}, Query has dim {}. Score will be 0.", 
                                    c.getId(), c.getEmbedding().length, queryVector.length);
                        }
                    })
                    .map(c -> new ScoredChunk(c, calculateCosineSimilarity(queryVector, c.getEmbedding())))
                    .filter(sc -> sc.score > 0.05)
                    .sorted((sc1, sc2) -> Double.compare(sc2.score, sc1.score))
                    .limit(topK * 4) // 先多取一点用于重排
                    .map(sc -> sc.chunk)
                    .collect(Collectors.toList());
            log.info("RAG Trace: Vector recall found {} candidates.", vectorRecall.size());
            if (vectorRecall.isEmpty() && !allCollectionChunks.isEmpty()) {
                log.error("RAG FATAL: Vector search returned 0 results. Check DIMENSION MISMATCH logs above.");
            }
        }

        // --- 阶段 3: 重排序 (Rerank) ---
        // 逻辑：合并所有候选 -> 计算最终加权分 -> 重新排序
        Map<String, ScoredChunk> candidateMap = new HashMap<>();
        
        // 向量分作为基础分
        if (queryVector != null) {
            for (KbChunk c : vectorRecall) {
                double baseScore = calculateCosineSimilarity(queryVector, c.getEmbedding());
                candidateMap.put(c.getId(), new ScoredChunk(c, baseScore));
            }
        }
        
        // 关键词分增加权重 (Bonus)
        for (KbChunk c : keywordRecall) {
            if (candidateMap.containsKey(c.getId())) {
                // 如果既是关键词匹配又是向量匹配，增加权重
                candidateMap.get(c.getId()).score += 0.3; 
            } else {
                // 仅关键词匹配，且没被向量召回发现（可能因为向量召回 Limit 限制），设一个较高的基础分
                // 如果能计算向量则计算，否则设个默认 0.5 作为基础分
                double s = (queryVector != null && c.getEmbedding() != null && c.getEmbedding().length > 0) 
                        ? calculateCosineSimilarity(queryVector, c.getEmbedding()) : 0.5;
                candidateMap.put(c.getId(), new ScoredChunk(c, s + 0.3));
            }
        }

        List<KbChunk> finalResults = candidateMap.values().stream()
                .sorted((sc1, sc2) -> Double.compare(sc2.score, sc1.score))
                .limit(topK)
                .map(sc -> sc.chunk)
                .collect(Collectors.toList());
        
        log.info("Final Reranked results count: {}", finalResults.size());
        if (!finalResults.isEmpty() && queryVector != null) {
             log.info("Top chunk content preview: [{}] (score: {})", 
                 finalResults.get(0).getContent().substring(0, Math.min(20, finalResults.get(0).getContent().length())),
                 candidateMap.get(finalResults.get(0).getId()).score);
        }

        return finalResults;
    }

    /**
     * 便捷分词器：将查询转换为关键字列表，用于兜底检索
     */
    private List<String> tokenizeQuery(String query) {
        if (query == null) return Collections.emptyList();
        // 简单正则：过滤标点符号，转换为小写，按空格拆分
        String cleaned = query.replaceAll("[\\p{Punct}\\s]+", " ");
        return Arrays.stream(cleaned.split(" "))
                .filter(s -> s.length() > 1) // 忽略单字符（如“的”、“了”）
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 计算余弦相似度
     */
    private double calculateCosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length || vectorA.length == 0) return 0.0;
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }
        if (normA == 0 || normB == 0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private static class ScoredChunk {
        KbChunk chunk;
        double score;
        ScoredChunk(KbChunk chunk, double score) {
            this.chunk = chunk;
            this.score = score;
        }
    }

    /**
     * 重新向量化集合中的所有分块 (用于模型更换后的迁移)
     */
    @Async
    public void reIndexCollection(String collectionId) {
        log.info("RAG: Starting re-indexing for collection {}", collectionId);
        List<String> docIds = documentRepository.findByCollectionId(collectionId).stream()
                .map(KbDocument::getId)
                .toList();
        
        for (String docId : docIds) {
            List<KbChunk> chunks = chunkRepository.findByDocumentId(docId);
            for (KbChunk chunk : chunks) {
                float[] newEmbedding = embeddingService.getEmbedding(chunk.getContent());
                if (newEmbedding != null) {
                    chunk.setEmbedding(newEmbedding);
                    chunk.setVectorStatus("DONE");
                    chunkRepository.save(chunk);
                }
            }
        }
        log.info("RAG: Re-indexing completed for collection {}", collectionId);
    }
}

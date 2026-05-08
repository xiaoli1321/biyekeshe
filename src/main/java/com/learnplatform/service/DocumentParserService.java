package com.learnplatform.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentParserService {

    private static final Logger log = LoggerFactory.getLogger(DocumentParserService.class);

    // 分割块最大长度和重叠度
    private static final int MAX_CHUNK_LENGTH = 500;
    private static final int OVERLAP_LENGTH = 50;

    /**
     * 根据文件格式解析出完整文本
     */
    public String extractText(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) return "";

        String text = "";
        try (InputStream is = file.getInputStream()) {
            if (filename.toLowerCase().endsWith(".pdf")) {
                log.info("Extracting text from PDF: {}", filename);
                // PDFBox 3.0.x uses Loader.loadPDF
                try (PDDocument document = Loader.loadPDF(is.readAllBytes())) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    text = stripper.getText(document);
                }
            } else if (filename.toLowerCase().endsWith(".docx")) {
                log.info("Extracting text from DOCX: {}", filename);
                try (XWPFDocument document = new XWPFDocument(is);
                     XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                    text = extractor.getText();
                }
            } else if (filename.toLowerCase().endsWith(".txt")) {
                log.info("Extracting text from TXT: {}", filename);
                byte[] bytes = is.readAllBytes();
                // 尝试检测是否为 UTF-8，如果不是则尝试 GBK (常见于中文 Windows)
                try {
                    text = new String(bytes, "UTF-8");
                    // 简单启发式：如果包含非法字符或全是乱码，可以进一步处理，这里简单处理
                } catch (Exception e) {
                    text = new String(bytes, "GBK");
                }
            } else {
                throw new IllegalArgumentException("Unsupported file type: " + filename);
            }
        }

        if (text == null || text.trim().isEmpty()) {
            log.warn("Extracted text is empty or null for file: {}", filename);
        } else {
            log.info("Successfully extracted {} characters from {}", text.length(), filename);
        }

        return text;
    }

    /**
     * 处理长文本，自研分块 (Chunking) 算法：
     * 1. 首先按自然段（回车）进行切分
     * 2. 如果某个自然段超过 MAX_CHUNK_LENGTH，则进行固定长度的滑动窗口截断（保留 OVERLAP_LENGTH 重叠）
     * 3. 将过短的自然段进行合并（可选，这里简单起见不做强合并，依赖 Embedding 自身的语义保留能力）
     */
    public List<String> chunkText(String fullText) {
        if (fullText == null || fullText.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<String> finalChunks = new ArrayList<>();
        // 按段落切分：\n 或 \r\n
        String[] paragraphs = fullText.split("\\r?\\n");

        StringBuilder currentBuffer = new StringBuilder();

        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) continue;

            // 检查如果加进去会不会超长
            if (currentBuffer.length() + trimmed.length() > MAX_CHUNK_LENGTH && currentBuffer.length() > 0) {
                // 如果缓冲里有东西并且加进去会超了，直接把旧的加到chunks里
                finalChunks.add(currentBuffer.toString());
                currentBuffer = new StringBuilder();
            }

            // 如果这一整段超级长，则按固定长度滑动切分
            if (trimmed.length() > MAX_CHUNK_LENGTH) {
                int start = 0;
                while (start < trimmed.length()) {
                    int end = Math.min(start + MAX_CHUNK_LENGTH, trimmed.length());
                    finalChunks.add(trimmed.substring(start, end));
                    if (end >= trimmed.length()) break;
                    // 向前移动：步进 = (切分长度 - 重叠长度)
                    start += (MAX_CHUNK_LENGTH - OVERLAP_LENGTH);
                }
            } else {
                if (currentBuffer.length() > 0) {
                    currentBuffer.append("\n");
                }
                currentBuffer.append(trimmed);
            }
        }
        
        if (currentBuffer.length() > 0) {
            finalChunks.add(currentBuffer.toString());
        }

        return finalChunks;
    }
}

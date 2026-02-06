package com.learnplatform.service;

import com.learnplatform.integration.python.PythonApiClient;
import org.springframework.stereotype.Service;

/**
 * 情绪分析服务（代理 Python 微服务）
 */
@Service
public class SentimentService {

    private final PythonApiClient pythonApiClient;

    public SentimentService(PythonApiClient pythonApiClient) {
        this.pythonApiClient = pythonApiClient;
    }

    public Object analyzeSentiment() {
        return pythonApiClient.post("/api/sentiment/analyze", null, null);
    }

    public Object listSentimentReports() {
        return pythonApiClient.get("/api/sentiment/reports", null);
    }

    public Object deleteSentimentReport(String filename) {
        return pythonApiClient.delete("/api/sentiment/reports/" + filename);
    }
}

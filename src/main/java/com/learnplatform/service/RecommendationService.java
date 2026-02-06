package com.learnplatform.service;

import com.learnplatform.integration.python.PythonApiClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 推荐服务（代理 Python 微服务）
 */
@Service
public class RecommendationService {

    private final PythonApiClient pythonApiClient;

    public RecommendationService(PythonApiClient pythonApiClient) {
        this.pythonApiClient = pythonApiClient;
    }

    public Object getShortTermStockRecommendations(Integer limit, Integer minScore) {
        return pythonApiClient.get("/api/recommend/stocks/short", buildRecommendParams(limit, minScore));
    }

    public Object getLongTermStockRecommendations(Integer limit, Integer minScore) {
        return pythonApiClient.get("/api/recommend/stocks/long", buildRecommendParams(limit, minScore));
    }

    public Object getShortTermFundRecommendations(Integer limit, Integer minScore) {
        return pythonApiClient.get("/api/recommend/funds/short", buildRecommendParams(limit, minScore));
    }

    public Object getLongTermFundRecommendations(Integer limit, Integer minScore) {
        return pythonApiClient.get("/api/recommend/funds/long", buildRecommendParams(limit, minScore));
    }

    public Object getLatestRecommendations() {
        return pythonApiClient.get("/api/recommend/latest", null);
    }

    private Map<String, Object> buildRecommendParams(Integer limit, Integer minScore) {
        Map<String, Object> params = new HashMap<>();
        params.put("limit", limit);
        params.put("min_score", minScore);
        return params;
    }
}

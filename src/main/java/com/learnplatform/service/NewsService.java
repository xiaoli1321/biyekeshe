package com.learnplatform.service;

import com.learnplatform.integration.python.PythonApiClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 新闻资讯服务（代理 Python 微服务）
 */
@Service
public class NewsService {

    private final PythonApiClient pythonApiClient;

    public NewsService(PythonApiClient pythonApiClient) {
        this.pythonApiClient = pythonApiClient;
    }

    public Object getNewsFeed(String category, Integer page, Integer pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("category", category);
        params.put("page", page);
        params.put("page_size", pageSize);
        return pythonApiClient.get("/api/news/feed", params);
    }

    public Object getHotNews(Integer limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("limit", limit);
        return pythonApiClient.get("/api/news/hot", params);
    }
}

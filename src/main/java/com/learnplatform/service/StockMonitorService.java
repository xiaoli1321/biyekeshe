package com.learnplatform.service;

import com.learnplatform.integration.python.PythonApiClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 股票监控服务（代理 Python 微服务）
 */
@Service
public class StockMonitorService {

    private final PythonApiClient pythonApiClient;

    public StockMonitorService(PythonApiClient pythonApiClient) {
        this.pythonApiClient = pythonApiClient;
    }

    public Object searchMarketStocks(String query) {
        Map<String, Object> params = new HashMap<>();
        params.put("query", query == null ? "" : query);
        return pythonApiClient.get("/api/market/stocks", params);
    }

    public Object getStockDetails(String code) {
        return pythonApiClient.get("/api/market/stocks/" + code + "/details", null);
    }

    public Object getStockHistory(String code) {
        return pythonApiClient.get("/api/market/stocks/" + code + "/history", null);
    }

    public Object getStockAiDiagnosis(String code) {
        return pythonApiClient.get("/api/stocks/" + code + "/ai-diagnosis", null);
    }
}

package com.learnplatform.service;

import com.learnplatform.integration.python.PythonApiClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基金分析服务（代理 Python 微服务）
 */
@Service
public class FundAnalysisService {

    private final PythonApiClient pythonApiClient;

    public FundAnalysisService(PythonApiClient pythonApiClient) {
        this.pythonApiClient = pythonApiClient;
    }

    public Object searchMarketFunds(String query) {
        Map<String, Object> params = new HashMap<>();
        params.put("q", query == null ? "" : query);
        return pythonApiClient.get("/api/market/funds", params);
    }

    public Object getFundMarketDetails(String code) {
        return pythonApiClient.get("/api/market/funds/" + code + "/details", null);
    }

    public Object getFundNavHistory(String code) {
        return pythonApiClient.get("/api/market/funds/" + code + "/nav", null);
    }

    public Object getFundDiagnosis(String code, Boolean forceRefresh) {
        Map<String, Object> params = new HashMap<>();
        params.put("force_refresh", forceRefresh);
        return pythonApiClient.get("/api/funds/" + code + "/diagnosis", params);
    }

    public Object getFundRiskMetrics(String code) {
        return pythonApiClient.get("/api/funds/" + code + "/risk-metrics", null);
    }

    public Object getFundDrawdownHistory(String code, Double threshold) {
        Map<String, Object> params = new HashMap<>();
        params.put("threshold", threshold);
        return pythonApiClient.get("/api/funds/" + code + "/drawdown-history", params);
    }

    public Object compareFunds(List<String> codes) {
        Map<String, Object> body = new HashMap<>();
        body.put("codes", codes);
        return pythonApiClient.post("/api/funds/compare", null, body);
    }
}

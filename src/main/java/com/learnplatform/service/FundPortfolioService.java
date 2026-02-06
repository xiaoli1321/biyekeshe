package com.learnplatform.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnplatform.integration.python.PythonServiceException;
import com.learnplatform.integration.python.PythonTokenManager;
import com.learnplatform.dto.request.PortfolioCreateRequest;
import com.learnplatform.dto.request.PortfolioUpdateRequest;
import com.learnplatform.dto.request.PortfolioPositionCreateRequest;
import com.learnplatform.dto.request.PortfolioTransactionCreateRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 基金投资组合管理服务（转发到 Python 微服务）
 */
@Service
public class FundPortfolioService {

    private final RestClient restClient;
    private final PythonTokenManager tokenManager;
    private final ObjectMapper objectMapper;

    public FundPortfolioService(RestClient pythonRestClient,
                                PythonTokenManager tokenManager,
                                ObjectMapper objectMapper) {
        this.restClient = pythonRestClient;
        this.tokenManager = tokenManager;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> listPortfolios() {
        return execute(() -> restClient.get()
                .uri("/api/portfolios")
                .headers(this::applyAuth)
                .retrieve()
                .body(Map.class));
    }

    public Map<String, Object> createPortfolio(PortfolioCreateRequest request) {
        return execute(() -> restClient.post()
                .uri("/api/portfolios")
                .headers(this::applyAuth)
                .body(request)
                .retrieve()
                .body(Map.class));
    }

    public Map<String, Object> getDefaultPortfolio() {
        return execute(() -> restClient.get()
                .uri("/api/portfolios/default")
                .headers(this::applyAuth)
                .retrieve()
                .body(Map.class));
    }

    public Map<String, Object> getPortfolio(long portfolioId) {
        return execute(() -> restClient.get()
                .uri("/api/portfolios/{id}", portfolioId)
                .headers(this::applyAuth)
                .retrieve()
                .body(Map.class));
    }

    public Map<String, Object> updatePortfolio(long portfolioId, PortfolioUpdateRequest request) {
        return execute(() -> restClient.put()
                .uri("/api/portfolios/{id}", portfolioId)
                .headers(this::applyAuth)
                .body(request)
                .retrieve()
                .body(Map.class));
    }

    public Map<String, Object> deletePortfolio(long portfolioId) {
        return execute(() -> restClient.delete()
                .uri("/api/portfolios/{id}", portfolioId)
                .headers(this::applyAuth)
                .retrieve()
                .body(Map.class));
    }

    public Map<String, Object> setDefaultPortfolio(long portfolioId) {
        return execute(() -> restClient.post()
                .uri("/api/portfolios/{id}/set-default", portfolioId)
                .headers(this::applyAuth)
                .retrieve()
                .body(Map.class));
    }

    public Map<String, Object> getPositions(long portfolioId, String assetType) {
        return execute(() -> restClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/api/portfolios/{id}/positions");
                    if (assetType != null && !assetType.isBlank()) {
                        builder.queryParam("asset_type", assetType);
                    }
                    return builder.build(portfolioId);
                })
                .headers(this::applyAuth)
                .retrieve()
                .body(Map.class));
    }

    public Map<String, Object> createPosition(long portfolioId, PortfolioPositionCreateRequest request) {
        return execute(() -> restClient.post()
                .uri("/api/portfolios/{id}/positions", portfolioId)
                .headers(this::applyAuth)
                .body(request)
                .retrieve()
                .body(Map.class));
    }

    public Map<String, Object> deletePosition(long portfolioId, long positionId) {
        return execute(() -> restClient.delete()
                .uri("/api/portfolios/{id}/positions/{positionId}", portfolioId, positionId)
                .headers(this::applyAuth)
                .retrieve()
                .body(Map.class));
    }

    public Map<String, Object> getTransactions(long portfolioId, String assetType, Integer limit, Integer offset) {
        return execute(() -> restClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/api/portfolios/{id}/transactions");
                    if (assetType != null && !assetType.isBlank()) {
                        builder.queryParam("asset_type", assetType);
                    }
                    if (limit != null) {
                        builder.queryParam("limit", limit);
                    }
                    if (offset != null) {
                        builder.queryParam("offset", offset);
                    }
                    return builder.build(portfolioId);
                })
                .headers(this::applyAuth)
                .retrieve()
                .body(Map.class));
    }

    public Map<String, Object> createTransaction(long portfolioId, PortfolioTransactionCreateRequest request) {
        return execute(() -> restClient.post()
                .uri("/api/portfolios/{id}/transactions", portfolioId)
                .headers(this::applyAuth)
                .body(request)
                .retrieve()
                .body(Map.class));
    }

    public Map<String, Object> deleteTransaction(long portfolioId, long transactionId) {
        return execute(() -> restClient.delete()
                .uri("/api/portfolios/{id}/transactions/{transactionId}", portfolioId, transactionId)
                .headers(this::applyAuth)
                .retrieve()
                .body(Map.class));
    }

    public Map<String, Object> getSummary(long portfolioId) {
        return execute(() -> restClient.get()
                .uri("/api/portfolios/{id}/summary", portfolioId)
                .headers(this::applyAuth)
                .retrieve()
                .body(Map.class));
    }

    private void applyAuth(HttpHeaders headers) {
        String token = tokenManager.getAccessToken();
        headers.setBearerAuth(token);
    }

    private Map<String, Object> execute(Supplier<Map<String, Object>> action) {
        try {
            Map<String, Object> result = action.get();
            if (result == null) {
                return Collections.emptyMap();
            }
            return result;
        } catch (RestClientResponseException ex) {
            String message = extractErrorMessage(ex);
            throw new PythonServiceException(message, ex.getStatusCode().value(), ex);
        } catch (ResourceAccessException ex) {
            throw new PythonServiceException("无法连接到 Python 服务，请检查服务是否启动", 503, ex);
        }
    }

    private String extractErrorMessage(RestClientResponseException ex) {
        String fallback = "Python 服务调用失败(" + ex.getStatusCode().value() + ")";
        String responseBody = ex.getResponseBodyAsString();
        if (responseBody == null || responseBody.isBlank()) {
            return fallback;
        }
        try {
            Map<String, Object> body = objectMapper.readValue(responseBody, new TypeReference<>() {
            });
            Object detail = body.get("detail");
            if (detail != null) {
                return detail.toString();
            }
            Object message = body.get("message");
            if (message != null) {
                return message.toString();
            }
            return fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }
}

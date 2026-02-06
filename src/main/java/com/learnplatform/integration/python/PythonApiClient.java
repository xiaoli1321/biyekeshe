package com.learnplatform.integration.python;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Python 微服务通用客户端
 */
@Component
public class PythonApiClient {

    private final RestClient restClient;
    private final PythonTokenManager tokenManager;
    private final ObjectMapper objectMapper;

    public PythonApiClient(RestClient pythonRestClient,
                           PythonTokenManager tokenManager,
                           ObjectMapper objectMapper) {
        this.restClient = pythonRestClient;
        this.tokenManager = tokenManager;
        this.objectMapper = objectMapper;
    }

    public Object get(String path, Map<String, ?> params) {
        return execute(() -> restClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path(path);
                    if (params != null) {
                        params.forEach((key, value) -> {
                            if (value != null) {
                                builder.queryParam(key, value);
                            }
                        });
                    }
                    return builder.build();
                })
                .headers(this::applyAuth)
                .retrieve()
                .body(Object.class));
    }

    public Object post(String path, Map<String, ?> params, Object body) {
        var request = restClient.post()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path(path);
                    if (params != null) {
                        params.forEach((key, value) -> {
                            if (value != null) {
                                builder.queryParam(key, value);
                            }
                        });
                    }
                    return builder.build();
                })
                .headers(this::applyAuth);

        if (body != null) {
            request.body(body);
        }

        return execute(() -> request.retrieve().body(Object.class));
    }

    public Object delete(String path) {
        return execute(() -> restClient.delete()
                .uri(path)
                .headers(this::applyAuth)
                .retrieve()
                .body(Object.class));
    }

    private void applyAuth(HttpHeaders headers) {
        headers.setBearerAuth(tokenManager.getAccessToken());
    }

    private Object execute(Supplier<Object> action) {
        try {
            Object result = action.get();
            return result != null ? result : Collections.emptyMap();
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

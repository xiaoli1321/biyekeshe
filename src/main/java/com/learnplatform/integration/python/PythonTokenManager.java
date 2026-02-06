package com.learnplatform.integration.python;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;

/**
 * Python 服务令牌管理器
 */
@Component
public class PythonTokenManager {

    private final RestClient restClient;
    private final PythonServiceProperties properties;
    private final Object lock = new Object();

    private volatile String accessToken;
    private volatile Instant expiresAt;

    public PythonTokenManager(RestClient pythonRestClient, PythonServiceProperties properties) {
        this.restClient = pythonRestClient;
        this.properties = properties;
    }

    public String getAccessToken() {
        Instant now = Instant.now();
        if (accessToken != null && expiresAt != null && now.isBefore(expiresAt.minus(properties.getTokenRefreshSkew()))) {
            return accessToken;
        }

        synchronized (lock) {
            Instant checkNow = Instant.now();
            if (accessToken != null && expiresAt != null && checkNow.isBefore(expiresAt.minus(properties.getTokenRefreshSkew()))) {
                return accessToken;
            }
            refreshToken();
            return accessToken;
        }
    }

    private void refreshToken() {
        try {
            PythonTokenResponse tokenResponse = login();
            cacheToken(tokenResponse);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 401 && properties.isAutoRegister()) {
                registerUser();
                PythonTokenResponse tokenResponse = login();
                cacheToken(tokenResponse);
                return;
            }
            throw ex;
        }
    }

    private PythonTokenResponse login() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", properties.getUsername());
        form.add("password", properties.getPassword());

        return restClient.post()
                .uri("/api/auth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(PythonTokenResponse.class);
    }

    private void registerUser() {
        PythonUserCreateRequest request = new PythonUserCreateRequest(
                properties.getUsername(),
                properties.getPassword(),
                null
        );

        restClient.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(PythonTokenResponse.class);
    }

    private void cacheToken(PythonTokenResponse tokenResponse) {
        if (tokenResponse == null || tokenResponse.accessToken() == null || tokenResponse.accessToken().isBlank()) {
            throw new IllegalStateException("Python 服务返回的令牌为空");
        }
        accessToken = tokenResponse.accessToken();
        expiresAt = Instant.now().plus(properties.getTokenTtl());
    }

    public void clearToken() {
        synchronized (lock) {
            accessToken = null;
            expiresAt = null;
        }
    }
}

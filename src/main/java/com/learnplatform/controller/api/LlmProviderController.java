package com.learnplatform.controller.api;

import com.learnplatform.dto.request.LlmProviderRequest;
import com.learnplatform.dto.response.LlmProviderDto;
import com.learnplatform.entity.LlmProvider;
import com.learnplatform.repository.LlmProviderRepository;
import com.learnplatform.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/llm-providers")
public class LlmProviderController {

    @Autowired
    private LlmProviderRepository providerRepository;

    @GetMapping
    public ResponseEntity<List<LlmProviderDto>> getProviders(@AuthenticationPrincipal UserPrincipal userDetails) {
        List<LlmProviderDto> result = new ArrayList<>();
        
        // 挂载默认系统配置 (System defaults)
        result.add(new LlmProviderDto("default-v3", "默认 Deepseek V3 大模型", "deepseek-chat", false, "系统直连"));
        result.add(new LlmProviderDto("default-r1", "默认 Deepseek R1 深度思考", "deepseek-reasoner", false, "系统直连"));
        result.add(new LlmProviderDto("default-glm4", "默认 GLM-4 大模型", "glm-4", false, "系统直连"));

        // 挂载用户自己的配置
        if (userDetails != null) {
            List<LlmProvider> customs = providerRepository.findByUserId(userDetails.getId());
            for (LlmProvider p : customs) {
                result.add(new LlmProviderDto(p.getId(), p.getName(), p.getModelAlias(), true, p.getBaseUrl()));
            }
        }
        return ResponseEntity.ok(result);
    }

    private void checkModelConnectivity(String baseUrl, String apiKey, String modelAlias) {
        String url = baseUrl == null || baseUrl.isBlank() ? "https://api.deepseek.com/v1" : baseUrl;
        WebClient client = WebClient.builder().baseUrl(url).build();
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelAlias);
        requestBody.put("messages", List.of(Map.of("role", "user", "content", "Hello!")));
        requestBody.put("max_tokens", 5);
        
        client.post()
              .uri("/chat/completions")
              .header("Authorization", "Bearer " + apiKey)
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(requestBody)
              .retrieve()
              .bodyToMono(String.class)
              .block();
    }

    @PostMapping
    public ResponseEntity<?> createProvider(@AuthenticationPrincipal UserPrincipal userDetails, @RequestBody LlmProviderRequest req) {
        // 先进行连通性打流测试
        try {
            checkModelConnectivity(req.getBaseUrl(), req.getApiKey(), req.getModelAlias());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("模型接口连通性测试失败！请核对 Base URL (不要带 /chat/completions)、模型代号以及您的 API Key 额度状态。错误信息: " + e.getMessage());
        }

        LlmProvider p = new LlmProvider();
        p.setUserId(userDetails.getId());
        p.setName(req.getName());
        p.setModelAlias(req.getModelAlias());
        p.setApiKey(req.getApiKey());
        p.setBaseUrl(req.getBaseUrl());
        LlmProvider saved = providerRepository.save(p);
        return ResponseEntity.ok(new LlmProviderDto(saved.getId(), saved.getName(), saved.getModelAlias(), true, saved.getBaseUrl()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProvider(@AuthenticationPrincipal UserPrincipal userDetails, @PathVariable String id) {
        providerRepository.findById(id).ifPresent(p -> {
            if (p.getUserId().equals(userDetails.getId())) {
                providerRepository.delete(p);
            }
        });
        return ResponseEntity.ok().build();
    }
}

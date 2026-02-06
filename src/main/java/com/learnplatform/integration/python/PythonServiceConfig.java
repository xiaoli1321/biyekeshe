package com.learnplatform.integration.python;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Python 微服务客户端配置
 */
@Configuration
public class PythonServiceConfig {

    @Bean
    public RestClient pythonRestClient(RestClient.Builder builder, PythonServiceProperties properties) {
        return builder
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}

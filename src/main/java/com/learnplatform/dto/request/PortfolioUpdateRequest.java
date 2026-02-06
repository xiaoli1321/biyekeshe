package com.learnplatform.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 更新基金投资组合请求
 */
public record PortfolioUpdateRequest(
        @JsonProperty("name")
        String name,

        @JsonProperty("description")
        String description,

        @JsonProperty("benchmark_code")
        String benchmarkCode,

        @JsonProperty("is_default")
        Boolean isDefault
) {
}

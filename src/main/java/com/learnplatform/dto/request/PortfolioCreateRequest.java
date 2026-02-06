package com.learnplatform.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * 创建基金投资组合请求
 */
public record PortfolioCreateRequest(
        @NotBlank(message = "组合名称不能为空")
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

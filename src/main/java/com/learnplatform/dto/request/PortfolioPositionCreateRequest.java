package com.learnplatform.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 创建投资组合持仓请求
 */
public record PortfolioPositionCreateRequest(
        @NotBlank(message = "资产类型不能为空")
        @JsonProperty("asset_type")
        String assetType,

        @NotBlank(message = "资产代码不能为空")
        @JsonProperty("asset_code")
        String assetCode,

        @JsonProperty("asset_name")
        String assetName,

        @NotNull(message = "持有份额不能为空")
        @Positive(message = "持有份额必须大于0")
        @JsonProperty("total_shares")
        Double totalShares,

        @NotNull(message = "平均成本不能为空")
        @Positive(message = "平均成本必须大于0")
        @JsonProperty("average_cost")
        Double averageCost,

        @JsonProperty("sector")
        String sector,

        @JsonProperty("notes")
        String notes
) {
}

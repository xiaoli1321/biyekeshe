package com.learnplatform.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 创建投资组合交易请求
 */
public record PortfolioTransactionCreateRequest(
        @NotBlank(message = "资产类型不能为空")
        @JsonProperty("asset_type")
        String assetType,

        @NotBlank(message = "资产代码不能为空")
        @JsonProperty("asset_code")
        String assetCode,

        @JsonProperty("asset_name")
        String assetName,

        @NotBlank(message = "交易类型不能为空")
        @JsonProperty("transaction_type")
        String transactionType,

        @NotNull(message = "交易份额不能为空")
        @Positive(message = "交易份额必须大于0")
        @JsonProperty("shares")
        Double shares,

        @NotNull(message = "交易价格不能为空")
        @Positive(message = "交易价格必须大于0")
        @JsonProperty("price")
        Double price,

        @JsonProperty("total_amount")
        Double totalAmount,

        @JsonProperty("fees")
        Double fees,

        @NotBlank(message = "交易日期不能为空")
        @JsonProperty("transaction_date")
        String transactionDate,

        @JsonProperty("notes")
        String notes
) {
}

package com.learnplatform.controller.api;

import com.learnplatform.dto.ApiResponse;
import com.learnplatform.integration.python.PythonServiceException;
import com.learnplatform.service.FundAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "基金分析", description = "基金分析相关API接口")
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FundAnalysisApiController {

    private final FundAnalysisService fundAnalysisService;

    public FundAnalysisApiController(FundAnalysisService fundAnalysisService) {
        this.fundAnalysisService = fundAnalysisService;
    }

    @Operation(summary = "搜索基金", description = "根据关键词搜索基金列表")
    @GetMapping("/market/funds")
    public ApiResponse<Object> searchMarketFunds(
            @Parameter(description = "基金代码或名称关键词")
            @RequestParam(defaultValue = "") String q
    ) {
        try {
            Object data = fundAnalysisService.searchMarketFunds(q);
            return ApiResponse.success(data, "获取基金列表成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取基金列表失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取基金列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "基金详情", description = "获取基金市场详情")
    @GetMapping("/market/funds/{code}/details")
    public ApiResponse<Object> getFundDetails(
            @Parameter(description = "基金代码", required = true)
            @PathVariable String code
    ) {
        try {
            Object data = fundAnalysisService.getFundMarketDetails(code);
            return ApiResponse.success(data, "获取基金详情成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取基金详情失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取基金详情失败: " + e.getMessage());
        }
    }

    @Operation(summary = "基金净值走势", description = "获取基金历史净值")
    @GetMapping("/market/funds/{code}/nav")
    public ApiResponse<Object> getFundNavHistory(
            @Parameter(description = "基金代码", required = true)
            @PathVariable String code
    ) {
        try {
            Object data = fundAnalysisService.getFundNavHistory(code);
            return ApiResponse.success(data, "获取基金净值成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取基金净值失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取基金净值失败: " + e.getMessage());
        }
    }

    @Operation(summary = "基金诊断", description = "获取基金五维诊断结果")
    @GetMapping("/funds/{code}/diagnosis")
    public ApiResponse<Object> getFundDiagnosis(
            @Parameter(description = "基金代码", required = true)
            @PathVariable String code,
            @Parameter(description = "是否强制刷新")
            @RequestParam(required = false) Boolean forceRefresh
    ) {
        try {
            Object data = fundAnalysisService.getFundDiagnosis(code, forceRefresh);
            return ApiResponse.success(data, "获取基金诊断成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取基金诊断失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取基金诊断失败: " + e.getMessage());
        }
    }

    @Operation(summary = "基金风险指标", description = "获取基金风险指标")
    @GetMapping("/funds/{code}/risk-metrics")
    public ApiResponse<Object> getFundRiskMetrics(
            @Parameter(description = "基金代码", required = true)
            @PathVariable String code
    ) {
        try {
            Object data = fundAnalysisService.getFundRiskMetrics(code);
            return ApiResponse.success(data, "获取基金风险指标成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取基金风险指标失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取基金风险指标失败: " + e.getMessage());
        }
    }

    @Operation(summary = "基金回撤分析", description = "获取基金回撤历史")
    @GetMapping("/funds/{code}/drawdown-history")
    public ApiResponse<Object> getFundDrawdownHistory(
            @Parameter(description = "基金代码", required = true)
            @PathVariable String code,
            @Parameter(description = "回撤阈值")
            @RequestParam(defaultValue = "0.05") Double threshold
    ) {
        try {
            Object data = fundAnalysisService.getFundDrawdownHistory(code, threshold);
            return ApiResponse.success(data, "获取基金回撤分析成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取基金回撤分析失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取基金回撤分析失败: " + e.getMessage());
        }
    }

    @Operation(summary = "基金对比", description = "对比多只基金表现")
    @PostMapping("/funds/compare")
    public ApiResponse<Object> compareFunds(
            @Valid @RequestBody FundCompareRequest request
    ) {
        try {
            Object data = fundAnalysisService.compareFunds(request.codes());
            return ApiResponse.success(data, "基金对比完成");
        } catch (PythonServiceException e) {
            return ApiResponse.error("基金对比失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("基金对比失败: " + e.getMessage());
        }
    }

    public record FundCompareRequest(List<String> codes) {
    }
}

package com.learnplatform.controller.api;

import com.learnplatform.dto.ApiResponse;
import com.learnplatform.integration.python.PythonServiceException;
import com.learnplatform.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI推荐", description = "AI 推荐相关API接口")
@RestController
@RequestMapping("/api/recommend")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RecommendationApiController {

    private final RecommendationService recommendationService;

    public RecommendationApiController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Operation(summary = "短期股票推荐", description = "获取短期股票推荐列表")
    @GetMapping("/stocks/short")
    public ApiResponse<Object> getShortStockRecommendations(
            @Parameter(description = "返回条数")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "最低评分")
            @RequestParam(required = false, name = "min_score") Integer minScore
    ) {
        try {
            Object data = recommendationService.getShortTermStockRecommendations(limit, minScore);
            return ApiResponse.success(data, "获取短期股票推荐成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取短期股票推荐失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取短期股票推荐失败: " + e.getMessage());
        }
    }

    @Operation(summary = "长期股票推荐", description = "获取长期股票推荐列表")
    @GetMapping("/stocks/long")
    public ApiResponse<Object> getLongStockRecommendations(
            @Parameter(description = "返回条数")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "最低评分")
            @RequestParam(required = false, name = "min_score") Integer minScore
    ) {
        try {
            Object data = recommendationService.getLongTermStockRecommendations(limit, minScore);
            return ApiResponse.success(data, "获取长期股票推荐成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取长期股票推荐失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取长期股票推荐失败: " + e.getMessage());
        }
    }

    @Operation(summary = "短期基金推荐", description = "获取短期基金推荐列表")
    @GetMapping("/funds/short")
    public ApiResponse<Object> getShortFundRecommendations(
            @Parameter(description = "返回条数")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "最低评分")
            @RequestParam(required = false, name = "min_score") Integer minScore
    ) {
        try {
            Object data = recommendationService.getShortTermFundRecommendations(limit, minScore);
            return ApiResponse.success(data, "获取短期基金推荐成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取短期基金推荐失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取短期基金推荐失败: " + e.getMessage());
        }
    }

    @Operation(summary = "长期基金推荐", description = "获取长期基金推荐列表")
    @GetMapping("/funds/long")
    public ApiResponse<Object> getLongFundRecommendations(
            @Parameter(description = "返回条数")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "最低评分")
            @RequestParam(required = false, name = "min_score") Integer minScore
    ) {
        try {
            Object data = recommendationService.getLongTermFundRecommendations(limit, minScore);
            return ApiResponse.success(data, "获取长期基金推荐成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取长期基金推荐失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取长期基金推荐失败: " + e.getMessage());
        }
    }

    @Operation(summary = "最新推荐汇总", description = "获取最新推荐汇总")
    @GetMapping("/latest")
    public ApiResponse<Object> getLatestRecommendations() {
        try {
            Object data = recommendationService.getLatestRecommendations();
            return ApiResponse.success(data, "获取最新推荐成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取最新推荐失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取最新推荐失败: " + e.getMessage());
        }
    }
}

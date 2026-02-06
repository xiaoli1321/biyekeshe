package com.learnplatform.controller.api;

import com.learnplatform.dto.ApiResponse;
import com.learnplatform.integration.python.PythonServiceException;
import com.learnplatform.service.SentimentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "情绪分析", description = "市场情绪分析相关API接口")
@RestController
@RequestMapping("/api/sentiment")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SentimentApiController {

    private final SentimentService sentimentService;

    public SentimentApiController(SentimentService sentimentService) {
        this.sentimentService = sentimentService;
    }

    @Operation(summary = "执行情绪分析", description = "触发市场情绪分析任务")
    @PostMapping("/analyze")
    public ApiResponse<Object> analyzeSentiment() {
        try {
            Object data = sentimentService.analyzeSentiment();
            return ApiResponse.success(data, "情绪分析完成");
        } catch (PythonServiceException e) {
            return ApiResponse.error("情绪分析失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("情绪分析失败: " + e.getMessage());
        }
    }

    @Operation(summary = "情绪报告列表", description = "获取历史情绪报告")
    @GetMapping("/reports")
    public ApiResponse<Object> listSentimentReports() {
        try {
            Object data = sentimentService.listSentimentReports();
            return ApiResponse.success(data, "获取情绪报告成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取情绪报告失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取情绪报告失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除情绪报告", description = "删除指定情绪报告")
    @DeleteMapping("/reports/{filename}")
    public ApiResponse<Object> deleteSentimentReport(
            @Parameter(description = "文件名", required = true)
            @PathVariable String filename
    ) {
        try {
            Object data = sentimentService.deleteSentimentReport(filename);
            return ApiResponse.success(data, "删除情绪报告成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("删除情绪报告失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("删除情绪报告失败: " + e.getMessage());
        }
    }
}

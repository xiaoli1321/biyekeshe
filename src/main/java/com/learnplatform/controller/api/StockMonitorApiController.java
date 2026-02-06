package com.learnplatform.controller.api;

import com.learnplatform.dto.ApiResponse;
import com.learnplatform.integration.python.PythonServiceException;
import com.learnplatform.service.StockMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "股票监控", description = "股票监控相关API接口")
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StockMonitorApiController {

    private final StockMonitorService stockMonitorService;

    public StockMonitorApiController(StockMonitorService stockMonitorService) {
        this.stockMonitorService = stockMonitorService;
    }

    @Operation(summary = "搜索股票", description = "根据关键词搜索股票列表")
    @GetMapping("/market/stocks")
    public ApiResponse<Object> searchMarketStocks(
            @Parameter(description = "股票代码或名称关键词")
            @RequestParam(defaultValue = "") String query
    ) {
        try {
            Object data = stockMonitorService.searchMarketStocks(query);
            return ApiResponse.success(data, "获取股票列表成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取股票列表失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取股票列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "股票详情", description = "获取股票实时行情与基础信息")
    @GetMapping("/market/stocks/{code}/details")
    public ApiResponse<Object> getStockDetails(
            @Parameter(description = "股票代码", required = true)
            @PathVariable String code
    ) {
        try {
            Object data = stockMonitorService.getStockDetails(code);
            return ApiResponse.success(data, "获取股票详情成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取股票详情失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取股票详情失败: " + e.getMessage());
        }
    }

    @Operation(summary = "股票历史行情", description = "获取股票历史行情数据")
    @GetMapping("/market/stocks/{code}/history")
    public ApiResponse<Object> getStockHistory(
            @Parameter(description = "股票代码", required = true)
            @PathVariable String code
    ) {
        try {
            Object data = stockMonitorService.getStockHistory(code);
            return ApiResponse.success(data, "获取股票历史行情成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取股票历史行情失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取股票历史行情失败: " + e.getMessage());
        }
    }

    @Operation(summary = "股票AI诊断", description = "获取股票AI诊断结果")
    @GetMapping("/stocks/{code}/ai-diagnosis")
    public ApiResponse<Object> getStockAiDiagnosis(
            @Parameter(description = "股票代码", required = true)
            @PathVariable String code
    ) {
        try {
            Object data = stockMonitorService.getStockAiDiagnosis(code);
            return ApiResponse.success(data, "获取股票AI诊断成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取股票AI诊断失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取股票AI诊断失败: " + e.getMessage());
        }
    }
}

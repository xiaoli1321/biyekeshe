package com.learnplatform.controller.api;

import com.learnplatform.dto.ApiResponse;
import com.learnplatform.dto.request.PortfolioCreateRequest;
import com.learnplatform.dto.request.PortfolioPositionCreateRequest;
import com.learnplatform.dto.request.PortfolioTransactionCreateRequest;
import com.learnplatform.dto.request.PortfolioUpdateRequest;
import com.learnplatform.integration.python.PythonServiceException;
import com.learnplatform.service.FundPortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "基金投资组合管理", description = "基金投资组合管理相关API接口")
@RestController
@RequestMapping("/api/portfolios")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PortfolioApiController {

    @Autowired
    private FundPortfolioService fundPortfolioService;

    @Operation(summary = "获取组合列表", description = "获取当前用户的所有组合")
    @GetMapping
    public ApiResponse<Map<String, Object>> listPortfolios() {
        try {
            Map<String, Object> data = fundPortfolioService.listPortfolios();
            return ApiResponse.success(data, "获取组合列表成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取组合列表失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取组合列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "创建组合", description = "创建新的投资组合")
    @PostMapping
    public ApiResponse<Map<String, Object>> createPortfolio(
            @Valid @RequestBody PortfolioCreateRequest request
    ) {
        try {
            Map<String, Object> data = fundPortfolioService.createPortfolio(request);
            return ApiResponse.success(data, "创建组合成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("创建组合失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("创建组合失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取默认组合", description = "获取当前用户默认组合")
    @GetMapping("/default")
    public ApiResponse<Map<String, Object>> getDefaultPortfolio() {
        try {
            Map<String, Object> data = fundPortfolioService.getDefaultPortfolio();
            return ApiResponse.success(data, "获取默认组合成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取默认组合失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取默认组合失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取组合详情", description = "根据组合ID获取组合详情")
    @GetMapping("/{portfolioId}")
    public ApiResponse<Map<String, Object>> getPortfolio(
            @Parameter(description = "组合ID", required = true)
            @PathVariable long portfolioId
    ) {
        try {
            Map<String, Object> data = fundPortfolioService.getPortfolio(portfolioId);
            return ApiResponse.success(data, "获取组合详情成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取组合详情失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取组合详情失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新组合", description = "更新组合信息")
    @PutMapping("/{portfolioId}")
    public ApiResponse<Map<String, Object>> updatePortfolio(
            @Parameter(description = "组合ID", required = true)
            @PathVariable long portfolioId,
            @Valid @RequestBody PortfolioUpdateRequest request
    ) {
        try {
            Map<String, Object> data = fundPortfolioService.updatePortfolio(portfolioId, request);
            return ApiResponse.success(data, "更新组合成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("更新组合失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("更新组合失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除组合", description = "删除指定组合")
    @DeleteMapping("/{portfolioId}")
    public ApiResponse<Map<String, Object>> deletePortfolio(
            @Parameter(description = "组合ID", required = true)
            @PathVariable long portfolioId
    ) {
        try {
            Map<String, Object> data = fundPortfolioService.deletePortfolio(portfolioId);
            return ApiResponse.success(data, "删除组合成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("删除组合失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("删除组合失败: " + e.getMessage());
        }
    }

    @Operation(summary = "设置默认组合", description = "将指定组合设为默认")
    @PostMapping("/{portfolioId}/set-default")
    public ApiResponse<Map<String, Object>> setDefaultPortfolio(
            @Parameter(description = "组合ID", required = true)
            @PathVariable long portfolioId
    ) {
        try {
            Map<String, Object> data = fundPortfolioService.setDefaultPortfolio(portfolioId);
            return ApiResponse.success(data, "设置默认组合成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("设置默认组合失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("设置默认组合失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取组合持仓", description = "获取组合内的持仓列表")
    @GetMapping("/{portfolioId}/positions")
    public ApiResponse<Map<String, Object>> getPositions(
            @Parameter(description = "组合ID", required = true)
            @PathVariable long portfolioId,
            @Parameter(description = "资产类型: fund 或 stock")
            @RequestParam(required = false) String assetType
    ) {
        try {
            Map<String, Object> data = fundPortfolioService.getPositions(portfolioId, assetType);
            return ApiResponse.success(data, "获取持仓成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取持仓失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取持仓失败: " + e.getMessage());
        }
    }

    @Operation(summary = "新增持仓", description = "为组合新增持仓")
    @PostMapping("/{portfolioId}/positions")
    public ApiResponse<Map<String, Object>> createPosition(
            @Parameter(description = "组合ID", required = true)
            @PathVariable long portfolioId,
            @Valid @RequestBody PortfolioPositionCreateRequest request
    ) {
        try {
            Map<String, Object> data = fundPortfolioService.createPosition(portfolioId, request);
            return ApiResponse.success(data, "新增持仓成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("新增持仓失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("新增持仓失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除持仓", description = "删除指定持仓")
    @DeleteMapping("/{portfolioId}/positions/{positionId}")
    public ApiResponse<Map<String, Object>> deletePosition(
            @Parameter(description = "组合ID", required = true)
            @PathVariable long portfolioId,
            @Parameter(description = "持仓ID", required = true)
            @PathVariable long positionId
    ) {
        try {
            Map<String, Object> data = fundPortfolioService.deletePosition(portfolioId, positionId);
            return ApiResponse.success(data, "删除持仓成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("删除持仓失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("删除持仓失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取交易记录", description = "获取组合交易记录")
    @GetMapping("/{portfolioId}/transactions")
    public ApiResponse<Map<String, Object>> getTransactions(
            @Parameter(description = "组合ID", required = true)
            @PathVariable long portfolioId,
            @Parameter(description = "资产类型: fund 或 stock")
            @RequestParam(required = false) String assetType,
            @Parameter(description = "每次返回条数")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "偏移量")
            @RequestParam(required = false) Integer offset
    ) {
        try {
            Map<String, Object> data = fundPortfolioService.getTransactions(portfolioId, assetType, limit, offset);
            return ApiResponse.success(data, "获取交易记录成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取交易记录失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取交易记录失败: " + e.getMessage());
        }
    }

    @Operation(summary = "新增交易记录", description = "新增交易记录并更新持仓")
    @PostMapping("/{portfolioId}/transactions")
    public ApiResponse<Map<String, Object>> createTransaction(
            @Parameter(description = "组合ID", required = true)
            @PathVariable long portfolioId,
            @Valid @RequestBody PortfolioTransactionCreateRequest request
    ) {
        try {
            Map<String, Object> data = fundPortfolioService.createTransaction(portfolioId, request);
            return ApiResponse.success(data, "新增交易记录成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("新增交易记录失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("新增交易记录失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除交易记录", description = "删除指定交易记录")
    @DeleteMapping("/{portfolioId}/transactions/{transactionId}")
    public ApiResponse<Map<String, Object>> deleteTransaction(
            @Parameter(description = "组合ID", required = true)
            @PathVariable long portfolioId,
            @Parameter(description = "交易ID", required = true)
            @PathVariable long transactionId
    ) {
        try {
            Map<String, Object> data = fundPortfolioService.deleteTransaction(portfolioId, transactionId);
            return ApiResponse.success(data, "删除交易记录成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("删除交易记录失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("删除交易记录失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取组合汇总", description = "获取组合总资产、收益和配置情况")
    @GetMapping("/{portfolioId}/summary")
    public ApiResponse<Map<String, Object>> getSummary(
            @Parameter(description = "组合ID", required = true)
            @PathVariable long portfolioId
    ) {
        try {
            Map<String, Object> data = fundPortfolioService.getSummary(portfolioId);
            return ApiResponse.success(data, "获取组合汇总成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取组合汇总失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取组合汇总失败: " + e.getMessage());
        }
    }
}

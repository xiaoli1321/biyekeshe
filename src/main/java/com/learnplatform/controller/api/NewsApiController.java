package com.learnplatform.controller.api;

import com.learnplatform.dto.ApiResponse;
import com.learnplatform.integration.python.PythonServiceException;
import com.learnplatform.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "新闻资讯", description = "新闻资讯相关API接口")
@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NewsApiController {

    private final NewsService newsService;

    public NewsApiController(NewsService newsService) {
        this.newsService = newsService;
    }

    @Operation(summary = "新闻流", description = "获取新闻资讯列表")
    @GetMapping("/feed")
    public ApiResponse<Object> getNewsFeed(
            @Parameter(description = "新闻分类")
            @RequestParam(defaultValue = "all") String category,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "20", name = "page_size") Integer pageSize
    ) {
        try {
            Object data = newsService.getNewsFeed(category, page, pageSize);
            return ApiResponse.success(data, "获取新闻流成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取新闻流失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取新闻流失败: " + e.getMessage());
        }
    }

    @Operation(summary = "热门新闻", description = "获取热门新闻")
    @GetMapping("/hot")
    public ApiResponse<Object> getHotNews(
            @Parameter(description = "返回条数")
            @RequestParam(defaultValue = "30") Integer limit
    ) {
        try {
            Object data = newsService.getHotNews(limit);
            return ApiResponse.success(data, "获取热门新闻成功");
        } catch (PythonServiceException e) {
            return ApiResponse.error("获取热门新闻失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取热门新闻失败: " + e.getMessage());
        }
    }
}

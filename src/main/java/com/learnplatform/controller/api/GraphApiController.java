package com.learnplatform.controller.api;

import com.learnplatform.dto.ApiResponse;
import com.learnplatform.dto.response.ConceptGraphDto;
import com.learnplatform.service.GraphService;
import com.learnplatform.util.DtoConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "知识图谱管理", description = "知识图谱相关的API接口")
@RestController
@RequestMapping("/api/graphs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GraphApiController {

    @Autowired
    private GraphService graphService;

    @Operation(summary = "获取课程概念图", description = "获取指定课程的知识概念图数据")
    @GetMapping("/concepts/{courseId}")
    public ApiResponse<ConceptGraphDto> getConceptGraph(
            @Parameter(description = "课程ID", required = true)
            @PathVariable String courseId,
            Authentication authentication
    ) {
        try {
            // 获取当前用户ID
            String userId = getCurrentUserId(authentication);

            // 构建概念图数据
            ConceptGraphDto conceptGraph = buildConceptGraph(courseId);

            return ApiResponse.success(conceptGraph, "获取概念图成功");
        } catch (Exception e) {
            return ApiResponse.error("获取概念图失败: " + e.getMessage());
        }
    }

    @Operation(summary = "生成可视化学习路径", description = "根据用户学习情况生成可视化学习路径")
    @PostMapping("/generate-path")
    public ApiResponse<ConceptGraphDto> generateLearningPath(
            @Parameter(description = "课程ID", required = true)
            @RequestParam String courseId,

            Authentication authentication
    ) {
        try {
            // 获取当前用户ID
            String userId = getCurrentUserId(authentication);

            // 生成学习路径图
            ConceptGraphDto learningPathGraph = buildLearningPathGraph(courseId, userId);

            return ApiResponse.success(learningPathGraph, "学习路径生成成功");
        } catch (Exception e) {
            return ApiResponse.error("生成学习路径失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取相关概念", description = "获取与指定概念相关的其他概念")
    @GetMapping("/related-concepts")
    public ApiResponse<List<String>> getRelatedConcepts(
            @Parameter(description = "概念名称", required = true)
            @RequestParam String conceptName,

            @Parameter(description = "课程ID", required = true)
            @RequestParam String courseId
    ) {
        try {
            // 获取相关概念列表（模拟数据）
            List<String> relatedConcepts = graphService.getRelatedConcepts(conceptName, courseId);

            if (relatedConcepts == null || relatedConcepts.isEmpty()) {
                // 如果没有相关概念，返回空列表
                relatedConcepts = new ArrayList<>();
            }

            return ApiResponse.success(relatedConcepts, "获取相关概念成功");
        } catch (Exception e) {
            return ApiResponse.error("获取相关概念失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取概念详情", description = "获取指定概念的详细信息")
    @GetMapping("/concept-detail")
    public ApiResponse<ConceptGraphDto.ConceptNodeDto> getConceptDetail(
            @Parameter(description = "概念ID", required = true)
            @RequestParam String conceptId
    ) {
        try {
            // 获取概念详情（模拟数据）
            ConceptGraphDto.ConceptNodeDto conceptDetail =
                    buildConceptDetail(conceptId);

            return ApiResponse.success(conceptDetail, "获取概念详情成功");
        } catch (Exception e) {
            return ApiResponse.error("获取概念详情失败: " + e.getMessage());
        }
    }

    // 私有辅助方法

    /**
     * 构建概念图数据
     * TODO: 这里应该调用GraphService的实际方法
     */
    private ConceptGraphDto buildConceptGraph(String courseId) {
        // 模拟概念节点数据
        List<ConceptGraphDto.ConceptNodeDto> nodes = new ArrayList<>();
        nodes.add(new ConceptGraphDto.ConceptNodeDto(
                "concept1", "数据结构", "数据结构基础概念", "基础概念", 1));
        nodes.add(new ConceptGraphDto.ConceptNodeDto(
                "concept2", "链表", "链表的定义和使用", "基础概念", 2));
        nodes.add(new ConceptGraphDto.ConceptNodeDto(
                "concept3", "栈", "栈的定义和操作", "基础概念", 2));
        nodes.add(new ConceptGraphDto.ConceptNodeDto(
                "concept4", "队列", "队列的类型和应用", "基础概念", 2));

        // 模拟关系边数据
        List<ConceptGraphDto.RelationEdgeDto> edges = new ArrayList<>();
        edges.add(new ConceptGraphDto.RelationEdgeDto(
                "edge1", "concept1", "concept2", "包含", "包含关系"));
        edges.add(new ConceptGraphDto.RelationEdgeDto(
                "edge2", "concept1", "concept3", "包含", "包含关系"));
        edges.add(new ConceptGraphDto.RelationEdgeDto(
                "edge3", "concept1", "concept4", "包含", "包含关系"));

        return new ConceptGraphDto(nodes, edges);
    }

    /**
     * 构建学习路径图
     */
    private ConceptGraphDto buildLearningPathGraph(String courseId, String userId) {
        // 类似概念图，但重点在学习顺序
        return buildConceptGraph(courseId);
    }

    /**
     * 构建概念详情
     */
    private ConceptGraphDto.ConceptNodeDto buildConceptDetail(String conceptId) {
        return new ConceptGraphDto.ConceptNodeDto(
                conceptId,
                "概念详情",
                "这是一个概念的详细信息",
                "详细信息",
                1
        );
    }

    /**
     * 获取当前认证用户ID
     * TODO: 在实际实现中，应从认证对象中获取
     */
    private String getCurrentUserId(Authentication authentication) {
        // 简化处理 - 在实际应用中应从认证对象中获取真实用户ID
        if (authentication != null && authentication.getName() != null) {
            return authentication.getName(); // 这里可以是用户ID或用户名
        }
        return "1"; // 默认用户ID，仅用于示例
    }
}
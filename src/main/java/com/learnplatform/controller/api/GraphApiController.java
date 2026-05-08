package com.learnplatform.controller.api;

import com.learnplatform.dto.ApiResponse;
import com.learnplatform.dto.GraphData;
import com.learnplatform.entity.Concept;
import com.learnplatform.security.UserPrincipal;
import com.learnplatform.service.GraphService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "知识图谱管理", description = "知识图谱相关的API接口")
@RestController
@RequestMapping("/api/graphs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GraphApiController {

    @Autowired
    private GraphService graphService;

    @Operation(summary = "获取课程完整知识图谱", description = "获取课程的概念节点+关系边+用户进度")
    @GetMapping("/{courseId}/full")
    public ApiResponse<GraphData> getFullGraph(
            @Parameter(description = "课程ID", required = true)
            @PathVariable String courseId,
            Authentication authentication
    ) {
        String userId = getCurrentUserId(authentication);
        GraphData data = graphService.getFullGraphData(courseId, userId);
        return ApiResponse.success(data, "获取知识图谱成功");
    }

    @Operation(summary = "获取课程概念图（无进度）", description = "获取指定课程的知识概念图基础数据")
    @GetMapping("/concepts/{courseId}")
    public ApiResponse<GraphData> getConceptGraph(
            @Parameter(description = "课程ID", required = true)
            @PathVariable String courseId
    ) {
        GraphData data = graphService.getCourseGraphData(courseId);
        return ApiResponse.success(data, "获取概念图成功");
    }

    @Operation(summary = "最短路径查询", description = "查找两个知识点之间的最短路径")
    @GetMapping("/shortest-path")
    public ApiResponse<List<String>> findShortestPath(
            @Parameter(description = "起始知识点ID", required = true)
            @RequestParam String from,
            @Parameter(description = "目标知识点ID", required = true)
            @RequestParam String to
    ) {
        List<String> path = graphService.findShortestPath(from, to);
        if (path.isEmpty()) {
            return ApiResponse.success(path, "两个知识点之间没有可达路径");
        }
        return ApiResponse.success(path, "最短路径查询成功");
    }

    @Operation(summary = "中心度分析", description = "计算课程知识图谱的节点中心度（简版PageRank）")
    @GetMapping("/{courseId}/centrality")
    public ApiResponse<Map<String, Double>> getCentrality(
            @Parameter(description = "课程ID", required = true)
            @PathVariable String courseId
    ) {
        Map<String, Double> centrality = graphService.calculateCentrality(courseId);
        return ApiResponse.success(centrality, "中心度计算成功");
    }

    @Operation(summary = "环路检测", description = "检测课程知识图谱中的环路（循环依赖）")
    @GetMapping("/{courseId}/cycles")
    public ApiResponse<List<List<String>>> detectCycles(
            @Parameter(description = "课程ID", required = true)
            @PathVariable String courseId
    ) {
        List<List<String>> cycles = graphService.detectCycles(courseId);
        return ApiResponse.success(cycles, cycles.isEmpty() ? "未检测到环路" : "检测到 " + cycles.size() + " 个环路");
    }

    @Operation(summary = "孤立知识点", description = "查找课程中没有关系的孤立知识点")
    @GetMapping("/{courseId}/isolated")
    public ApiResponse<List<String>> getIsolatedConcepts(
            @Parameter(description = "课程ID", required = true)
            @PathVariable String courseId
    ) {
        List<String> isolated = graphService.findIsolatedConcepts(courseId);
        return ApiResponse.success(isolated, "查到 " + isolated.size() + " 个孤立知识点");
    }

    @Operation(summary = "知识点详情上下文", description = "获取知识点的先修/后续/关联关系完整上下文")
    @GetMapping("/concept/{conceptId}/detail")
    public ApiResponse<Map<String, Object>> getConceptDetail(
            @Parameter(description = "知识点ID", required = true)
            @PathVariable String conceptId
    ) {
        Map<String, Object> context = graphService.getConceptContext(conceptId);
        return ApiResponse.success(context, "获取知识点详情成功");
    }

    @Operation(summary = "个性化学习路径", description = "根据用户学习进度生成拓扑排序的学习路径")
    @GetMapping("/learning-path/{courseId}")
    public ApiResponse<List<Concept>> getLearningPath(
            @Parameter(description = "课程ID", required = true)
            @PathVariable String courseId,
            Authentication authentication
    ) {
        String userId = getCurrentUserId(authentication);
        List<Concept> path = graphService.generateLearningPath(userId, courseId);
        return ApiResponse.success(path, "学习路径生成成功，共 " + path.size() + " 个知识点待学习");
    }

    @Operation(summary = "知识点的先修知识点", description = "获取某个知识点的直接先修知识点列表")
    @GetMapping("/concept/{conceptId}/prerequisites")
    public ApiResponse<List<Concept>> getPrerequisites(
            @Parameter(description = "知识点ID", required = true)
            @PathVariable String conceptId
    ) {
        List<Concept> prerequisites = graphService.getDirectPrerequisites(conceptId);
        return ApiResponse.success(prerequisites, "获取先修知识点成功");
    }

    @Operation(summary = "搜索相关概念", description = "按名称模糊搜索课程内的相关概念")
    @GetMapping("/related-concepts")
    public ApiResponse<List<String>> getRelatedConcepts(
            @Parameter(description = "概念名称", required = true)
            @RequestParam String conceptName,
            @Parameter(description = "课程ID", required = true)
            @RequestParam String courseId
    ) {
        List<String> related = graphService.getRelatedConcepts(conceptName, courseId);
        return ApiResponse.success(related, "获取相关概念成功");
    }

    private String getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }

        String name = authentication.getName();
        if (name != null && !"anonymousUser".equals(name)) {
            return name;
        }

        return null;
    }
}

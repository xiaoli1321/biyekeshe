package com.learnplatform.controller.api;

import com.learnplatform.dto.ApiResponse;
import com.learnplatform.dto.response.CourseDto;
import com.learnplatform.dto.response.DashboardStatsDto;
import com.learnplatform.dto.response.LearningPathDto;
import com.learnplatform.entity.Course;
import com.learnplatform.security.UserPrincipal;
import com.learnplatform.service.CourseService;
import com.learnplatform.service.LearningPathService;
import com.learnplatform.service.ProgressService;
import com.learnplatform.util.DtoConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "仪表盘管理", description = "用户仪表盘相关的API接口")
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardApiController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private ProgressService progressService;

    @Autowired
    private LearningPathService learningPathService;

    @Operation(summary = "获取仪表盘统计数据", description = "获取用户的学习统计数据")
    @GetMapping("/stats")
    public ApiResponse<DashboardStatsDto> getDashboardStats(Authentication authentication) {
        // 获取当前用户ID
        String userId = getCurrentUserId(authentication);

        // 基础统计信息
        long totalCourses = courseService.countPublishedCourses();

        // 获取用户的学习进度
        List<com.learnplatform.entity.Progress> userProgress = progressService.getUserProgress(userId);

        // 统计已完成章节数
        long completedChapters = userProgress.stream()
                .filter(p -> p.isCompleted())
                .mapToInt(p -> 1)
                .sum();

        // 统计学习时长
        ProgressService.UserStats stats = progressService.getUserLearningStats(userId);
        long totalMinutes = stats.totalMinutes();
        int totalHours = (int) (totalMinutes / 60);

        // 获取推荐课程
        List<Course> recommendedCourses = learningPathService.getRecommendedCourses(userId);
        if (recommendedCourses == null || recommendedCourses.isEmpty()) {
            // 如果没有推荐，返回热门课程
            recommendedCourses = courseService.getPopularCourses(
                    org.springframework.data.domain.PageRequest.of(0, 3));
        }

        List<CourseDto> recommendedCourseDtos = DtoConverter.convertList(
                recommendedCourses, CourseDto::fromCourseBasic);

        // 构建响应
        DashboardStatsDto dashboardStats = new DashboardStatsDto(
                totalCourses,
                completedChapters,
                totalHours,
                recommendedCourseDtos
        );

        return ApiResponse.success(dashboardStats, "获取统计数据成功");
    }

    @Operation(summary = "获取推荐课程", description = "获取个性化推荐的课程列表")
    @GetMapping("/recommended-courses")
    public ApiResponse<List<CourseDto>> getRecommendedCourses(Authentication authentication) {
        // 获取当前用户ID
        String userId = getCurrentUserId(authentication);

        List<Course> recommendedCourses = learningPathService.getRecommendedCourses(userId);
        if (recommendedCourses == null || recommendedCourses.isEmpty()) {
            // 如果没有推荐，返回热门课程
            recommendedCourses = courseService.getPopularCourses(
                    org.springframework.data.domain.PageRequest.of(0, 6));
        }

        List<CourseDto> recommendedCourseDtos = DtoConverter.convertList(
                recommendedCourses, CourseDto::fromCourseBasic);

        return ApiResponse.success(recommendedCourseDtos, "获取推荐课程成功");
    }

    @Operation(summary = "获取学习路径", description = "获取指定课程的学习路径")
    @GetMapping("/learning-path/{courseId}")
    public ApiResponse<LearningPathDto> getLearningPath(
            @Parameter(description = "课程ID", required = true)
            @PathVariable String courseId,
            Authentication authentication
    ) {
        try {
            // 获取当前用户ID
            String userId = getCurrentUserId(authentication);

            // 验证课程存在
            Course course = courseService.findCourseById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("课程未找到"));

            // 获取学习路径
            LearningPathService.LearningPath learningPath =
                    learningPathService.generatePersonalizedLearningPath(userId, courseId);

            List<String> suggestions = learningPathService.getLearningSuggestions(userId, courseId);

            // 转换DTO
            List<com.learnplatform.dto.response.ChapterDto> path = learningPath.getPathConcepts().stream()
                    .map(concept -> {
                        com.learnplatform.dto.response.ChapterDto dto = new com.learnplatform.dto.response.ChapterDto();
                        dto.setId(concept.getId());
                        dto.setCourseId(courseId);
                        dto.setTitle(concept.getName());
                        dto.setDescription(concept.getDescription());
                        dto.setContent("");
                        dto.setOrderIndex(0);
                        dto.setEstimatedMinutes(0);
                        return dto;
                    })
                    .toList();

            LearningPathDto response = new LearningPathDto(
                    courseId,
                    course.getName(),
                    path,
                    suggestions
            );

            return ApiResponse.success(response, "获取学习路径成功");
        } catch (Exception e) {
            return ApiResponse.error("获取学习路径失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取学习建议", description = "获取针对当前用户的学习建议")
    @GetMapping("/learning-suggestions")
    public ApiResponse<List<String>> getLearningSuggestions(
            @Parameter(description = "课程ID", required = true)
            @RequestParam String courseId,
            Authentication authentication
    ) {
        try {
            // 获取当前用户ID
            String userId = getCurrentUserId(authentication);

            List<String> suggestions = learningPathService.getLearningSuggestions(userId, courseId);

            return ApiResponse.success(suggestions, "获取学习建议成功");
        } catch (Exception e) {
            return ApiResponse.error("获取学习建议失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前认证用户ID
     * TODO: 在实际实现中，应从认证对象中获取
     */
    private String getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }

        if (authentication.getName() != null && !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName();
        }

        return null;
    }
}

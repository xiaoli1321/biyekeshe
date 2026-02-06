package com.learnplatform.controller.api;

import com.learnplatform.dto.request.CourseSearchRequest;
import com.learnplatform.dto.response.*;
import com.learnplatform.dto.ApiResponse;
import com.learnplatform.dto.PageResponse;
import com.learnplatform.entity.Course;
import com.learnplatform.security.UserPrincipal;
import com.learnplatform.service.CourseService;
import com.learnplatform.service.ChapterService;
import com.learnplatform.service.ProgressService;
import com.learnplatform.util.DtoConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "课程管理", description = "课程相关的API接口")
@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CourseApiController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private ProgressService progressService;

    @Operation(summary = "获取课程列表", description = "根据分页和筛选条件获取课程列表")
    @GetMapping
    public ApiResponse<PageResponse<CourseDto>> getCourses(
            @Parameter(description = "搜索关键词（课程名称、描述等）")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "难度等级")
            @RequestParam(required = false) Course.DifficultyLevel difficulty,

            @Parameter(description = "页码（从0开始）")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "12") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Course> courses;

        if (keyword != null && !keyword.trim().isEmpty()) {
            courses = courseService.searchCourses(keyword, pageable);
        } else if (difficulty != null) {
            List<Course> courseList = courseService.getCoursesByDifficultyLevel(difficulty);
            // 手动分页（简单实现）
            int start = page * size;
            int end = Math.min(start + size, courseList.size());
            java.util.List<Course> pageContent = new java.util.ArrayList<>();
            if (start < courseList.size()) {
                pageContent = courseList.subList(start, end);
            }
            Page<Course> pageResult = new org.springframework.data.domain.PageImpl<>(
                    pageContent, pageable, courseList.size());
            courses = pageResult;
        } else {
            courses = courseService.getPublishedCourses(pageable);
        }

        PageResponse<CourseDto> response = DtoConverter.convertPage(courses, CourseDto::fromCourseBasic);
        return ApiResponse.success(response, "获取课程列表成功");
    }

    @Operation(summary = "获取全部课程（管理员）", description = "获取所有课程（包含未发布）")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CourseDto>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        List<CourseDto> courseDtos = DtoConverter.convertList(courses, CourseDto::fromCourseBasic);
        return ApiResponse.success(courseDtos, "获取全部课程成功");
    }

    @Operation(summary = "获取课程详情", description = "根据课程ID获取课程详细信息（包括章节列表）")
    @GetMapping("/{id}")
    public ApiResponse<CourseDto> getCourseById(
            @Parameter(description = "课程ID", required = true)
            @PathVariable String id
    ) {
        Optional<Course> courseOpt = courseService.findCourseById(id);
        if (courseOpt.isEmpty()) {
            return ApiResponse.error("课程未找到", "COURSE_NOT_FOUND");
        }

        CourseDto courseDto = CourseDto.fromCourse(courseOpt.get());
        return ApiResponse.success(courseDto, "获取课程详情成功");
    }

    @Operation(summary = "获取课程章节列表", description = "根据课程ID获取课程的章节列表")
    @GetMapping("/{courseId}/chapters")
    public ApiResponse<List<ChapterDto>> getChaptersByCourse(
            @Parameter(description = "课程ID", required = true)
            @PathVariable String courseId
    ) {
        // 验证课程存在
        if (courseService.findCourseById(courseId).isEmpty()) {
            return ApiResponse.error("课程未找到", "COURSE_NOT_FOUND");
        }

        List<com.learnplatform.entity.Chapter> chapters = chapterService.getChaptersByCourseOrdered(courseId);
        List<ChapterDto> chapterDtos = DtoConverter.convertList(chapters, ChapterDto::fromChapter);

        return ApiResponse.success(chapterDtos, "获取章节列表成功");
    }

    @Operation(summary = "获取课程进度", description = "获取当前用户的课程学习进度")
    @GetMapping("/{courseId}/progress")
    public ApiResponse<CourseProgressDto> getCourseProgress(
            @Parameter(description = "课程ID", required = true)
            @PathVariable String courseId,
            org.springframework.security.core.Authentication authentication
    ) {
        try {
            String userId = getCurrentUserId(authentication);
            if (userId == null) {
                return ApiResponse.error("用户未登录", "NOT_AUTHENTICATED");
            }

            if (courseService.findCourseById(courseId).isEmpty()) {
                return ApiResponse.error("课程未找到", "COURSE_NOT_FOUND");
            }

            CourseProgressDto progressDto = progressService.getCourseProgressSummary(userId, courseId);
            return ApiResponse.success(progressDto, "获取课程进度成功");
        } catch (Exception e) {
            return ApiResponse.error("获取课程进度失败: " + e.getMessage());
        }
    }

    @Operation(summary = "创建课程", description = "创建新课程（需要管理员权限）")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CourseDto> createCourse(
            @Parameter(description = "课程信息", required = true)
            @RequestBody Course course
    ) {
        try {
            // 设置默认值
            course.setPublished(false);

            Course savedCourse = courseService.createCourse(course);
            CourseDto courseDto = CourseDto.fromCourse(savedCourse);

            return ApiResponse.success(courseDto, "课程创建成功");
        } catch (Exception e) {
            return ApiResponse.error("创建课程失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新课程", description = "更新课程信息（需要管理员权限）")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CourseDto> updateCourse(
            @Parameter(description = "课程ID", required = true)
            @PathVariable String id,

            @Parameter(description = "课程更新信息", required = true)
            @RequestBody Course course
    ) {
        try {
            // 验证课程存在
            if (courseService.findCourseById(id).isEmpty()) {
                return ApiResponse.error("课程未找到", "COURSE_NOT_FOUND");
            }

            course.setId(id);
            Course updatedCourse = courseService.updateCourse(course);
            CourseDto courseDto = CourseDto.fromCourse(updatedCourse);

            return ApiResponse.success(courseDto, "课程更新成功");
        } catch (Exception e) {
            return ApiResponse.error("更新课程失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除课程", description = "删除指定课程（需要管理员权限）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteCourse(
            @Parameter(description = "课程ID", required = true)
            @PathVariable String id
    ) {
        try {
            // 验证课程存在
            if (courseService.findCourseById(id).isEmpty()) {
                return ApiResponse.error("课程未找到", "COURSE_NOT_FOUND");
            }

            courseService.deleteCourse(id);
            return ApiResponse.success(null, "课程删除成功");
        } catch (Exception e) {
            return ApiResponse.error("删除课程失败: " + e.getMessage());
        }
    }

    @Operation(summary = "发布课程", description = "发布或取消发布课程（需要管理员权限）")
    @PostMapping("/{id}/toggle-publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CourseDto> togglePublishStatus(
            @Parameter(description = "课程ID", required = true)
            @PathVariable String id
    ) {
        try {
            Course course = courseService.togglePublishStatus(id);
            CourseDto courseDto = CourseDto.fromCourseBasic(course);

            String message = course.isPublished() ? "课程已发布" : "课程已取消发布";
            return ApiResponse.success(courseDto, message);
        } catch (Exception e) {
            return ApiResponse.error("操作失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取热门课程", description = "获取热门推荐课程")
    @GetMapping("/popular")
    public ApiResponse<List<CourseDto>> getPopularCourses(
            @Parameter(description = "获取数量")
            @RequestParam(defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Course> popularCourses = courseService.getPopularCourses(pageable);
        List<CourseDto> courseDtos = DtoConverter.convertList(popularCourses, CourseDto::fromCourseBasic);

        return ApiResponse.success(courseDtos, "获取热门课程成功");
    }

    @Operation(summary = "搜索课程", description = "根据关键词搜索课程")
    @GetMapping("/search")
    public ApiResponse<PageResponse<CourseDto>> searchCourses(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam String keyword,

            @Parameter(description = "页码（从0开始）")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "12") int size
    ) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ApiResponse.error("搜索关键词不能为空", "INVALID_KEYWORD");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Course> searchResults = courseService.searchCourses(keyword, pageable);
        PageResponse<CourseDto> response = DtoConverter.convertPage(searchResults, CourseDto::fromCourseBasic);

        return ApiResponse.success(response, "搜索完成");
    }

    /**
     * 获取当前认证用户ID
     */
    private String getCurrentUserId(org.springframework.security.core.Authentication authentication) {
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

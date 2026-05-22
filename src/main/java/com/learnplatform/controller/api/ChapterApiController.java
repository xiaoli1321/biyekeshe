package com.learnplatform.controller.api;

import com.learnplatform.dto.ApiResponse;
import com.learnplatform.dto.response.ChapterDto;
import com.learnplatform.dto.response.ChapterProgressDto;
import com.learnplatform.dto.response.ConceptGraphDto;
import com.learnplatform.entity.Chapter;
import com.learnplatform.entity.Concept;
import com.learnplatform.entity.Progress;
import com.learnplatform.security.UserPrincipal;
import com.learnplatform.service.ChapterService;
import com.learnplatform.service.ConceptService;
import com.learnplatform.service.GraphService;
import com.learnplatform.service.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "章节管理", description = "章节相关的API接口")
@RestController
@RequestMapping("/api/chapters")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChapterApiController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private ProgressService progressService;

    @Autowired
    private GraphService graphService;

    @Autowired
    private ConceptService conceptService;

    @Operation(summary = "获取章节详情", description = "根据章节ID获取章节详细信息")
    @GetMapping("/{id}")
    public ApiResponse<ChapterDto> getChapterById(
            @Parameter(description = "章节ID", required = true)
            @PathVariable String id
    ) {
        Optional<Chapter> chapterOpt = chapterService.findChapterById(id);
        if (chapterOpt.isEmpty()) {
            return ApiResponse.error("章节未找到", "CHAPTER_NOT_FOUND");
        }

        ChapterDto chapterDto = ChapterDto.fromChapter(chapterOpt.get());
        return ApiResponse.success(chapterDto, "获取章节详情成功");
    }

    @Operation(summary = "创建章节", description = "为指定课程创建新章节（需要管理员权限）")
    @PostMapping("/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ChapterDto> createChapter(
            @Parameter(description = "课程ID", required = true)
            @PathVariable String courseId,

            @Parameter(description = "章节信息", required = true)
            @RequestBody Chapter chapter
    ) {
        try {
            com.learnplatform.entity.Course course = new com.learnplatform.entity.Course();
            course.setId(courseId);
            chapter.setCourse(course);
            Chapter savedChapter = chapterService.createChapter(chapter);
            ChapterDto chapterDto = ChapterDto.fromChapter(savedChapter);

            return ApiResponse.success(chapterDto, "章节创建成功");
        } catch (Exception e) {
            return ApiResponse.error("创建章节失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新章节", description = "更新章节信息（需要管理员权限）")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ChapterDto> updateChapter(
            @Parameter(description = "章节ID", required = true)
            @PathVariable String id,

            @Parameter(description = "章节更新信息", required = true)
            @RequestBody Chapter chapter
    ) {
        try {
            // 验证章节存在
            if (chapterService.findChapterById(id).isEmpty()) {
                return ApiResponse.error("章节未找到", "CHAPTER_NOT_FOUND");
            }

            chapter.setId(id);
            Chapter updatedChapter = chapterService.updateChapter(chapter);
            ChapterDto chapterDto = ChapterDto.fromChapter(updatedChapter);

            return ApiResponse.success(chapterDto, "章节更新成功");
        } catch (Exception e) {
            return ApiResponse.error("更新章节失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除章节", description = "删除指定章节（需要管理员权限）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteChapter(
            @Parameter(description = "章节ID", required = true)
            @PathVariable String id
    ) {
        try {
            // 验证章节存在
            if (chapterService.findChapterById(id).isEmpty()) {
                return ApiResponse.error("章节未找到", "CHAPTER_NOT_FOUND");
            }

            chapterService.deleteChapter(id);
            return ApiResponse.success(null, "章节删除成功");
        } catch (Exception e) {
            return ApiResponse.error("删除章节失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新学习进度", description = "记录用户学习进度")
    @PostMapping("/{chapterId}/progress")
    public ApiResponse<String> updateLearningProgress(
            @Parameter(description = "章节ID", required = true)
            @PathVariable String chapterId,

            @Parameter(description = "进度信息", required = true)
            @RequestBody ProgressUpdateRequest request,
            Authentication authentication
    ) {
        try {
            String userId = getCurrentUserId(authentication);
            if (userId == null) {
                return ApiResponse.error("用户未登录", "NOT_AUTHENTICATED");
            }

            // 处理进度更新
            boolean completed = request.isCompleted();
            int elapsedMinutes = request.getElapsedMinutes();
            Progress.ProgressStatus targetStatus = request.resolveStatus();

            if (targetStatus != null) {
                progressService.updateProgress(userId, chapterId, targetStatus, elapsedMinutes);
            } else {
                progressService.updateProgress(userId, chapterId, completed, elapsedMinutes);
            }

            String message = switch (targetStatus != null ? targetStatus : (completed ? Progress.ProgressStatus.COMPLETED : Progress.ProgressStatus.IN_PROGRESS)) {
                case COMPLETED -> "章节已完成";
                case MASTERED -> "章节已标记为已掌握";
                case NOT_STARTED -> "章节已重置为未开始";
                default -> "学习进度已保存";
            };
            return ApiResponse.success(null, message);
        } catch (Exception e) {
            return ApiResponse.error("更新进度失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新学习笔记", description = "保存用户对应的章节随堂笔记")
    @PutMapping("/{chapterId}/notes")
    public ApiResponse<Void> updateChapterNotes(
            @Parameter(description = "章节ID", required = true)
            @PathVariable String chapterId,

            @Parameter(description = "笔记内容", required = true)
            @RequestBody String notes,
            Authentication authentication
    ) {
        try {
            String userId = getCurrentUserId(authentication);
            if (userId == null) {
                return ApiResponse.error("用户未登录", "NOT_AUTHENTICATED");
            }

            progressService.updateNotes(userId, chapterId, notes);
            return ApiResponse.success(null, "笔记已保存");
        } catch (Exception e) {
            return ApiResponse.error("保存笔记失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取章节进度", description = "获取当前用户的章节学习进度")
    @GetMapping("/{chapterId}/progress")
    public ApiResponse<ChapterProgressDto> getChapterProgress(
            @Parameter(description = "章节ID", required = true)
            @PathVariable String chapterId,
            Authentication authentication
    ) {
        try {
            String userId = getCurrentUserId(authentication);
            if (userId == null) {
                return ApiResponse.error("用户未登录", "NOT_AUTHENTICATED");
            }

            ChapterProgressDto progressDto = progressService.getChapterProgressSummary(userId, chapterId);
            return ApiResponse.success(progressDto, "获取章节进度成功");
        } catch (Exception e) {
            return ApiResponse.error("获取章节进度失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取章节前置知识点", description = "获取学习本章节前建议先复习的概念知识点")
    @GetMapping("/{id}/prerequisites")
    public ApiResponse<List<ConceptGraphDto.ConceptNodeDto>> getChapterPrerequisites(
            @Parameter(description = "章节ID", required = true)
            @PathVariable String id
    ) {
        try {
            Optional<Chapter> chapterOpt = chapterService.findChapterById(id);
            if (chapterOpt.isEmpty()) {
                return ApiResponse.error("章节未找到", "CHAPTER_NOT_FOUND");
            }

            Chapter chapter = chapterOpt.get();
            List<Concept> chapterConcepts = conceptService.getConceptsByChapter(chapter.getId());
            
            // 收集所有关联概念的前置知识点
            List<Concept> allPrerequisites = new ArrayList<>();
            for (Concept concept : chapterConcepts) {
                List<Concept> prerequisites = graphService.getDirectPrerequisites(concept.getId());
                allPrerequisites.addAll(prerequisites);
            }

            // 去重并转换为 DTO
            List<ConceptGraphDto.ConceptNodeDto> dtos = allPrerequisites.stream()
                    .distinct()
                    .map(c -> new ConceptGraphDto.ConceptNodeDto(
                            c.getId(),
                            c.getName(),
                            c.getDescription(),
                            "核心概念", // 默认分类
                            c.getDifficultyLevel() != null ? c.getDifficultyLevel() : 1
                    ))
                    .collect(Collectors.toList());

            return ApiResponse.success(dtos, "获取前置知识点成功");
        } catch (Exception e) {
            return ApiResponse.error("获取前置知识点失败: " + e.getMessage());
        }
    }

    // 内部类：进度更新请求
    public static class ProgressUpdateRequest {
        private boolean completed;
        private int elapsedMinutes;
        private String status;

        public ProgressUpdateRequest() {
        }

        public ProgressUpdateRequest(boolean completed, int elapsedMinutes) {
            this.completed = completed;
            this.elapsedMinutes = elapsedMinutes;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public int getElapsedMinutes() {
            return elapsedMinutes;
        }

        public void setElapsedMinutes(int elapsedMinutes) {
            this.elapsedMinutes = elapsedMinutes;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Progress.ProgressStatus resolveStatus() {
            if (status == null || status.isBlank()) {
                return null;
            }
            return Progress.ProgressStatus.valueOf(status);
        }
    }

    /**
     * 获取当前认证用户ID
     */
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

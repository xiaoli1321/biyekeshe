package com.learnplatform.controller;

import com.learnplatform.entity.Course;
import com.learnplatform.service.CourseService;
import com.learnplatform.service.ProgressService;
import com.learnplatform.service.LearningPathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仪表盘控制器
 */
@Controller
public class DashboardController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private ProgressService progressService;

    @Autowired
    private LearningPathService learningPathService;

    /**
     * 用户仪表盘主页
     */
    @GetMapping("/dashboard")
    public String showDashboard(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        // 获取当前用户ID（简化处理）
        // 获取当前用户ID（简化处理）
        String userId = "1"; // 在实际应用中应该从认证对象中获取

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

        // 添加统计数据到模型
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("completedChapters", completedChapters);
        model.addAttribute("totalHours", totalHours);

        // 获取推荐课程
        List<Course> recommendedCourses = learningPathService.getRecommendedCourses(userId);
        if (recommendedCourses == null || recommendedCourses.isEmpty()) {
            // 如果没有推荐，返回热门课程
            recommendedCourses = courseService.getPopularCourses(
                    org.springframework.data.domain.PageRequest.of(0, 3));
        }

        model.addAttribute("recommendedCourses", recommendedCourses);

        return "dashboard/index";
    }

    /**
     * 显示学习路径
     */
    @GetMapping("/learning-path/{courseId}")
    public String showLearningPath(@PathVariable String courseId, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String userId = "1"; // 在实际应用中应该从认证对象中获取

        Course course = courseService.findCourseById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程未找到"));

        LearningPathService.LearningPath learningPath = learningPathService
                .generatePersonalizedLearningPath(userId, courseId);

        List<String> suggestions = learningPathService.getLearningSuggestions(userId, courseId);

        model.addAttribute("course", course);
        model.addAttribute("learningPath", learningPath);
        model.addAttribute("suggestions", suggestions);

        return "dashboard/learning-path";
    }
}
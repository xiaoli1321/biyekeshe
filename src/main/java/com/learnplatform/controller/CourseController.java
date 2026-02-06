package com.learnplatform.controller;

import com.learnplatform.entity.Course;
import com.learnplatform.entity.Chapter;
import com.learnplatform.service.CourseService;
import com.learnplatform.service.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * 课程控制器
 */
@Controller
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private ChapterService chapterService;

    /**
     * 课程列表（分页）
     */
    @GetMapping
    public String listCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Course.DifficultyLevel difficulty,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Course> courses;

        if (keyword != null && !keyword.isEmpty()) {
            courses = courseService.searchCourses(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else if (difficulty != null) {
            List<Course> courseList = courseService.getCoursesByDifficultyLevel(difficulty);
            // 手动分页（简单实现）
            int start = page * size;
            int end = Math.min(start + size, courseList.size());
            List<Course> pageContent = new ArrayList<>();
            if (start < courseList.size()) {
                pageContent = courseList.subList(start, end);
            }
            courses = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, courseList.size());
            model.addAttribute("difficulty", difficulty);
        } else {
            courses = courseService.getPublishedCourses(pageable);
        }

        model.addAttribute("courses", courses);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", courses.getTotalPages());
        model.addAttribute("totalItems", courses.getTotalElements());

        return "courses/list";
    }

    /**
     * 课程详情
     */
    @GetMapping("/{id}")
    public String courseDetail(@PathVariable String id, Model model) {
        Course course = courseService.findCourseById(id)
                .orElseThrow(() -> new IllegalArgumentException("课程未找到"));
        List<Chapter> chapters = chapterService.getChaptersByCourseOrdered(id);

        model.addAttribute("course", course);
        model.addAttribute("chapters", chapters);

        return "courses/detail";
    }

    /**
     * 显示创建课程表单
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("action", "create");
        return "courses/form";
    }

    /**
     * 创建课程
     */
    @PostMapping("/create")
    public String createCourse(@Valid @ModelAttribute("course") Course course,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "courses/form";
        }

        course.setPublished(false);
        courseService.createCourse(course);
        redirectAttributes.addFlashAttribute("message", "课程创建成功！");
        return "redirect:/courses/" + course.getId();
    }

    /**
     * 显示编辑课程表单
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable String id, Model model) {
        Course course = courseService.findCourseById(id)
                .orElseThrow(() -> new IllegalArgumentException("课程未找到"));
        model.addAttribute("course", course);
        model.addAttribute("action", "edit");
        return "courses/form";
    }

    /**
     * 更新课程
     */
    @PostMapping("/{id}/edit")
    public String updateCourse(@PathVariable String id,
                              @Valid @ModelAttribute("course") Course course,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "courses/form";
        }

        course.setId(id);
        courseService.updateCourse(course);
        redirectAttributes.addFlashAttribute("message", "课程更新成功！");
        return "redirect:/courses/" + id;
    }

    /**
     * 发布/取消发布课程
     */
    @PostMapping("/{id}/toggle-publish")
    public String togglePublishStatus(@PathVariable String id, RedirectAttributes redirectAttributes) {
        courseService.togglePublishStatus(id);
        Course course = courseService.findCourseById(id).orElseThrow();
        String message = course.isPublished() ? "课程已发布" : "课程已取消发布";
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/courses/" + id;
    }

    /**
     * 删除课程
     */
    @PostMapping("/{id}/delete")
    public String deleteCourse(@PathVariable String id, RedirectAttributes redirectAttributes) {
        courseService.deleteCourse(id);
        redirectAttributes.addFlashAttribute("message", "课程已删除！");
        return "redirect:/courses";
    }
}
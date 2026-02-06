package com.learnplatform.controller;

import com.learnplatform.entity.Course;
import com.learnplatform.entity.Concept;
import com.learnplatform.service.CourseService;
import com.learnplatform.service.GraphService;
import com.learnplatform.service.ConceptService;
import com.learnplatform.dto.GraphData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识图谱控制器
 */
@Controller
@RequestMapping("/knowledge-graph")
public class GraphController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private GraphService graphService;

    @Autowired
    private ConceptService conceptService;

    /**
     * 显示知识图谱页面
     */
    @GetMapping
    public String showKnowledgeGraph(Model model) {
        List<Course> courses = courseService.getPublishedCourses();
        model.addAttribute("courses", courses);
        return "knowledge-graph/view";
    }

    /**
     * 获取指定课程的知识图谱数据（页面）
     */
    @GetMapping("/course/{courseId}")
    public String showCourseGraph(@PathVariable String courseId, Model model) {
        Course course = courseService.findCourseById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程未找到"));
        model.addAttribute("course", course);
        model.addAttribute("courses", courseService.getPublishedCourses());
        return "knowledge-graph/view";
    }

    /**
     * 获取知识图谱API数据
     */
    @GetMapping("/api/graph/{courseId}")
    @ResponseBody
    public GraphData getGraphData(@PathVariable String courseId) {
        return graphService.getCourseGraphData(courseId);
    }

    /**
     * 获取概念的先修知识点API
     */
    @GetMapping("/api/concepts/{conceptId}/prerequisites")
    @ResponseBody
    public List<Concept> getPrerequisites(@PathVariable String conceptId) {
        return graphService.getAllPrerequisites(conceptId);
    }

    /**
     * 获取概念的依赖概念API
     */
    @GetMapping("/api/concepts/{conceptId}/dependents")
    @ResponseBody
    public List<Concept> getDependents(@PathVariable String conceptId) {
        return graphService.getAllDependents(conceptId);
    }

    /**
     * 检验概念是否可学习
     */
    @GetMapping("/api/concepts/{conceptId}/canStudy")
    @ResponseBody
    public boolean canStudy(@PathVariable String conceptId) {
        return graphService.canStudyConcept(conceptId);
    }

    /**
     * 获取核心理念点
     */
    @GetMapping("/api/key-concepts/{courseId}")
    @ResponseBody
    public List<Concept> getKeyConcepts(@PathVariable String courseId) {
        return graphService.getKeyConcepts(courseId);
    }

    /**
     * 生成个性化学习路径
     */
    @GetMapping("/api/learning-path/{courseId}/{userId}")
    @ResponseBody
    public List<Concept> getLearningPath(@PathVariable String courseId, @PathVariable String userId) {
        return graphService.generateLearningPath(userId, courseId);
    }
}
package com.learnplatform.service;

import com.learnplatform.entity.Course;
import com.learnplatform.repository.CourseRepository;
import com.learnplatform.repository.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 课程业务服务
 */
@Service
@Transactional
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private ConceptProgressService conceptProgressService;

    /**
     * 创建课程
     */
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    /**
     * 更新课程
     */
    public Course updateCourse(Course course) {
        return courseRepository.save(course);
    }

    /**
     * 查找课程
     */
    public Optional<Course> findCourseById(String id) {
        return courseRepository.findById(id);
    }

    /**
     * 获取所有课程
     */
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    /**
     * 根据发布状态获取课程
     */
    public List<Course> getPublishedCourses() {
        return courseRepository.findByPublished(true);
    }

    /**
     * 根据发布状态分页获取课程
     */
    public Page<Course> getPublishedCourses(Pageable pageable) {
        return courseRepository.findByPublished(true, pageable);
    }

    /**
     * 根据难度等级获取课程
     */
    public List<Course> getCoursesByDifficultyLevel(Course.DifficultyLevel difficulty) {
        return courseRepository.findByDifficultyLevel(difficulty);
    }

    /**
     * 根据讲师获取课程
     */
    public List<Course> getCoursesByInstructor(String instructor) {
        return courseRepository.findByInstructor(instructor);
    }

    /**
     * 发布/取消发布课程
     */
    public Course togglePublishStatus(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程未找到"));
        course.setPublished(!course.isPublished());
        return courseRepository.save(course);
    }

    /**
     * 发布课程
     */
    public Course publishCourse(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程未找到"));
        course.setPublished(true);
        return courseRepository.save(course);
    }

    /**
     * 取消发布课程
     */
    public Course unpublishCourse(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程未找到"));
        course.setPublished(false);
        return courseRepository.save(course);
    }

    /**
     * 删除课程
     */
    public void deleteCourse(String id) {
        chapterService.deleteChaptersByCourse(id);
        conceptService.deleteConceptsByCourse(id);
        progressRepository.deleteByCourse_Id(id);
        conceptProgressService.deleteByCourseId(id);
        courseRepository.deleteById(id);
    }

    /**
     * 搜索课程
     */
    public Page<Course> searchCourses(String keyword, Pageable pageable) {
        return courseRepository.searchCourses(keyword, pageable);
    }

    /**
     * 通过标签搜索课程
     */
    public List<Course> searchCoursesByTag(String tag) {
        return courseRepository.findByTag(tag);
    }

    /**
     * 获取热门课程
     */
    public List<Course> getPopularCourses(Pageable pageable) {
        return courseRepository.findPopularCourses(pageable);
    }

    /**
     * 统计课程总数
     */
    public long countTotalCourses() {
        return courseRepository.count();
    }

    /**
     * 统计已发布课程数
     */
    public long countPublishedCourses() {
        return courseRepository.countByPublishedTrue();
    }

    /**
     * 获取课程统计信息
     */
    public List<Object[]> getCourseStatistics() {
        return courseRepository.countCoursesByDifficultyLevel();
    }
}

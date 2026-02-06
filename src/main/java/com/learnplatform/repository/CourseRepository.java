package com.learnplatform.repository;

import com.learnplatform.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 课程数据访问层
 */
@Repository
public interface CourseRepository extends MongoRepository<Course, String> {

    /**
     * 根据发布状态查找课程
     */
    List<Course> findByPublished(boolean published);

    /**
     * 根据发布状态分页查找课程
     */
    Page<Course> findByPublished(boolean published, Pageable pageable);

    /**
     * 根据难度等级查找课程
     */
    List<Course> findByDifficultyLevel(Course.DifficultyLevel difficultyLevel);

    /**
     * 根据讲师查找课程
     */
    List<Course> findByInstructor(String instructor);

    /**
     * 根据发布状态和难度等级查找课程
     */
    List<Course> findByPublishedAndDifficultyLevel(boolean published, Course.DifficultyLevel difficultyLevel);

    /**
     * 根据标签搜索课程
     */
    @Query("{$or: [{tags: {$regex: ?0, $options: 'i'}}, {name: {$regex: ?0, $options: 'i'}}]}")
    List<Course> findByTag(String tag);

    /**
     * 根据关键词搜索课程
     */
    @Query("{$or: [{name: {$regex: ?0, $options: 'i'}}, {description: {$regex: ?0, $options: 'i'}}, {tags: {$regex: ?0, $options: 'i'}}]}")
    Page<Course> searchCourses(String keyword, Pageable pageable);

    /**
     * 查找已发布的课程，按创建时间倒序
     */
    List<Course> findByPublishedTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 根据难度等级和发布时间查询课程
     */
    List<Course> findByDifficultyLevelAndPublishedTrueOrderByCreatedAtDesc(
            Course.DifficultyLevel difficulty, Pageable pageable);

    /**
     * 统计已发布的课程数
     */
    long countByPublishedTrue();



    /**
     * 按难度统计课程数量
     */
    @Aggregation(pipeline = {
        "{ $match: { published: true } }",
        "{ $group: { _id: '$difficultyLevel', count: { $sum: 1 } } }"
    })
    List<Object[]> countCoursesByDifficultyLevel();

    /**
     * 查询热门课程（基于章节数量）
     * MongoDB中SIZE操作在Query中不直接支持，通常需要存储size字段或者使用Aggregation
     */
    @Aggregation(pipeline = {
        "{ $match: { published: true } }",
        "{ $project: { name: 1, description: 1, difficultyLevel: 1, createdAt: 1, published: 1, coverImageUrl: 1, instructor: 1, estimatedHours: 1, tags: 1, chapters: 1, chapterCount: { $size: { $ifNull: ['$chapters', []] } } } }",
        "{ $sort: { chapterCount: -1 } }"
    })
    List<Course> findPopularCourses(Pageable pageable);

    /**
     * 按讲师统计课程数量
     */
    @Aggregation(pipeline = {
        "{ $match: { published: true, instructor: { $ne: null } } }",
        "{ $group: { _id: '$instructor', count: { $sum: 1 } } }"
    })
    List<Object[]> countCoursesByInstructor();

    /**
     * 根据预估学习时长范围查找课程
     */
    List<Course> findByEstimatedHoursBetweenAndPublishedTrue(Integer minHours, Integer maxHours, Pageable pageable);
}
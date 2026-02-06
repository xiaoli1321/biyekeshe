package com.learnplatform.repository;

import com.learnplatform.entity.Chapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 章节数据访问层
 */
@Repository
public interface ChapterRepository extends MongoRepository<Chapter, String> {

    /**
     * 根据课程查找章节（按顺序排序）
     */
    List<Chapter> findByCourse_IdOrderByOrderIndex(String courseId);

    /**
     * 根据课程和顺序索引查找后续章节
     */
    List<Chapter> findByCourse_IdAndOrderIndexGreaterThanOrderByOrderIndex(String courseId, Integer orderIndex);

    /**
     * 根据课程和顺序索引查找前一章节
     */
    Optional<Chapter> findByCourse_IdAndOrderIndexLessThanOrderByOrderIndexDesc(String courseId, Integer orderIndex);

    /**
     * 根据课程和顺序索引查找章节
     */
    Optional<Chapter> findByCourse_IdAndOrderIndex(String courseId, Integer orderIndex);

    /**
     * 根据章节类型查找章节
     */
    List<Chapter> findByType(Chapter.ChapterType type);

    /**
     * 根据课程和类型查找章节
     */
    List<Chapter> findByCourse_IdAndType(String courseId, Chapter.ChapterType type);

    /**
     * 根据课程统计章节数
     */
    long countByCourse_Id(String courseId);

    /**
     * 查找课程的第一章节
     */
    List<Chapter> findByCourse_IdOrderByOrderIndexAsc(String courseId, Pageable pageable);

    /**
     * 查找课程的最后章节
     */
    List<Chapter> findByCourse_IdOrderByOrderIndexDesc(String courseId, Pageable pageable);

    /**
     * 根据章节类型统订数量
     */
    @Query(value = "{}", fields = "{type: 1}")
    List<Chapter> findAllTypes(); // Note: Simplified, actual aggregation might be better

    /**
     * 根据课程ID和章节名模糊查找
     */
    @Query("{ 'course.$id' : ?0, 'title' : { $regex: ?1, $options: 'i' } }")
    List<Chapter> findByCourseIdAndTitleContaining(String courseId, String title);

    /**
     * 统计可跳过章节数
     */
    long countByCourse_IdAndSkippableTrue(String courseId);

    /**
     * 根据课程查找可跳过章节
     */
    List<Chapter> findByCourse_IdAndSkippableTrueOrderByOrderIndex(String courseId);
}

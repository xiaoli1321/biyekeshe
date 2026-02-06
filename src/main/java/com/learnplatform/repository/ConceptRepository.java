package com.learnplatform.repository;

import com.learnplatform.entity.Concept;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 知识点数据访问层
 */
@Repository
public interface ConceptRepository extends MongoRepository<Concept, String> {

    /**
     * 根据课程查找知识点
     */
    List<Concept> findByCourse_Id(String courseId);

    /**
     * 根据章节查找知识点
     */
    List<Concept> findByChapter_Id(String chapterId);

    /**
     * 根据难度等级查找知识点
     */
    List<Concept> findByDifficultyLevel(Integer difficultyLevel);

    /**
     * 根据重要程度权重查找知识点
     */
    List<Concept> findByImportanceWeightGreaterThanEqual(Integer weight);

    /**
     * 根据课程和难度等级查找知识点
     */
    List<Concept> findByCourse_IdAndDifficultyLevel(String courseId, Integer difficultyLevel);

    /**
     * 按重要程度权重排序查找知识点
     */
    List<Concept> findByCourse_IdOrderByImportanceWeightDesc(String courseId);

    /**
     * 根据课程名称模糊搜索
     */
    @Query("{ 'course.$id' : ?0, 'name' : { $regex: ?1, $options: 'i' } }")
    List<Concept> findByCourseIdAndNameContaining(String courseId, String keyword);

    /**
     * 按课程统订知识点数量
     */
    long countByCourse_Id(String courseId);

    /**
     * 按章节统计知识点数量
     */
    long countByChapter_Id(String chapterId);

    /**
     * 查询课程的核心理念点（高重要程度）
     */
    List<Concept> findByCourse_IdAndImportanceWeightGreaterThanEqual(String courseId, Integer minWeight);

    /**
     * 查找高级知识点
     */
    List<Concept> findByCourse_IdAndDifficultyLevelGreaterThanEqual(String courseId, Integer minLevel);

    /**
     * 查找初级知识点
     */
    List<Concept> findByCourse_IdAndDifficultyLevelLessThanEqual(String courseId, Integer maxLevel);

    /**
     * 查找有先修关系的关键节点
     */
    @Query("{ 'course.$id' : ?0, 'outgoingRelationships' : { $exists: true, $not: { $size: 0 } } }")
    List<Concept> findKeyNodesWithOutgoingRelationships(String courseId);
}

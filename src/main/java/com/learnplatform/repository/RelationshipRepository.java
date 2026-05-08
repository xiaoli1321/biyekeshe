package com.learnplatform.repository;

import com.learnplatform.entity.Relationship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 关系数据访问层
 */
@Repository
public interface RelationshipRepository extends MongoRepository<Relationship, String> {

    /**
     * 根据源知识点查找关系
     */
    List<Relationship> findByFromConcept_Id(String fromConceptId);

    /**
     * 根据目标知识点查找关系
     */
    List<Relationship> findByToConcept_Id(String toConceptId);

    /**
     * 根据源节点和关系类型查找关系
     */
    List<Relationship> findByFromConcept_IdAndRelationshipType(String fromConceptId, Relationship.RelationshipType type);

    /**
     * 根据目标节点和关系类型查找关系
     */
    List<Relationship> findByToConcept_IdAndRelationshipType(String toConceptId, Relationship.RelationshipType type);

    /**
     * 根据关系类型查找关系
     */
    List<Relationship> findByRelationshipType(Relationship.RelationshipType type);

    /**
     * 根据源节点、目标节点和关系类型查找关系
     */
    Optional<Relationship> findByFromConcept_IdAndToConcept_IdAndRelationshipType(
            String fromConceptId, String toConceptId, Relationship.RelationshipType type);

    /**
     * 查找两个概念之间的所有关系
     */
    List<Relationship> findByFromConcept_IdAndToConcept_Id(String fromConceptId, String toConceptId);

    /**
     * 统计源节点的出度关系数
     */
    long countByFromConcept_Id(String conceptId);

    /**
     * 统计目标节点的入度关系数
     */
    long countByToConcept_Id(String conceptId);

    /**
     * 查找课程的所有关系（修复 DBRef 查询）
     */
    @Query("{ '$or': [ { 'fromConcept.$id': { $in: ?0 } }, { 'toConcept.$id': { $in: ?0 } } ] }")
    List<Relationship> findByFromConcept_IdInOrToConcept_IdIn(List<String> conceptIds);

    /**
     * 按关系类型排序的关系查询
     */
    List<Relationship> findByRelationshipTypeOrderByWeightDesc(Relationship.RelationshipType type);

    /**
     * 查找高权重的关系
     */
    List<Relationship> findByWeightGreaterThanEqual(Double weight);
}

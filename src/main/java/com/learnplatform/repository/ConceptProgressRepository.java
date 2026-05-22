package com.learnplatform.repository;

import com.learnplatform.entity.ConceptProgress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConceptProgressRepository extends MongoRepository<ConceptProgress, String> {

    List<ConceptProgress> findByUser_IdAndCourse_Id(String userId, String courseId);

    ConceptProgress findByUser_IdAndConcept_Id(String userId, String conceptId);

    void deleteByCourse_Id(String courseId);

    void deleteByChapter_Id(String chapterId);

    void deleteByConcept_Id(String conceptId);
}

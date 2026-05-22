package com.learnplatform.service;

import com.learnplatform.entity.Concept;
import com.learnplatform.entity.ConceptProgress;
import com.learnplatform.entity.User;
import com.learnplatform.repository.ConceptRepository;
import com.learnplatform.repository.ConceptProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ConceptProgressService {

    @Autowired
    private ConceptProgressRepository conceptProgressRepository;

    @Autowired
    private ConceptRepository conceptRepository;

    public List<ConceptProgress> getUserCourseConceptProgress(String userId, String courseId) {
        if (userId == null || userId.isBlank() || courseId == null || courseId.isBlank()) {
            return List.of();
        }
        return conceptProgressRepository.findByUser_IdAndCourse_Id(userId, courseId);
    }

    public ConceptProgress getConceptProgress(String userId, String conceptId) {
        if (userId == null || userId.isBlank() || conceptId == null || conceptId.isBlank()) {
            return null;
        }
        return conceptProgressRepository.findByUser_IdAndConcept_Id(userId, conceptId);
    }

    public ConceptProgress updateProgress(String userId, String conceptId, ConceptProgress.ProgressStatus targetStatus, int studyMinutes) {
        Concept concept = conceptRepository.findById(conceptId)
                .orElseThrow(() -> new IllegalArgumentException("知识点未找到"));

        ConceptProgress progress = conceptProgressRepository.findByUser_IdAndConcept_Id(userId, conceptId);
        if (progress == null) {
            progress = new ConceptProgress();

            User user = new User();
            user.setId(userId);

            progress.setUser(user);
            progress.setConcept(concept);
            progress.setCourse(concept.getCourse());
            progress.setChapter(concept.getChapter());
            progress.setStartedAt(LocalDateTime.now());
        }

        ConceptProgress.ProgressStatus effectiveStatus = targetStatus != null
                ? targetStatus
                : ConceptProgress.ProgressStatus.IN_PROGRESS;

        progress.setStatus(effectiveStatus);
        if (effectiveStatus == ConceptProgress.ProgressStatus.NOT_STARTED) {
            progress.setCompletedAt(null);
        } else if (effectiveStatus == ConceptProgress.ProgressStatus.IN_PROGRESS) {
            progress.setCompletedAt(null);
        } else if (effectiveStatus == ConceptProgress.ProgressStatus.COMPLETED
                || effectiveStatus == ConceptProgress.ProgressStatus.MASTERED) {
            progress.setCompletedAt(LocalDateTime.now());
        }

        int safeMinutes = Math.max(studyMinutes, 0);
        Integer existingMinutes = progress.getStudyDurationMinutes();
        progress.setStudyDurationMinutes((existingMinutes != null ? existingMinutes : 0) + safeMinutes);

        return conceptProgressRepository.save(progress);
    }

    public void deleteByCourseId(String courseId) {
        conceptProgressRepository.deleteByCourse_Id(courseId);
    }

    public void deleteByChapterId(String chapterId) {
        conceptProgressRepository.deleteByChapter_Id(chapterId);
    }

    public void deleteByConceptId(String conceptId) {
        conceptProgressRepository.deleteByConcept_Id(conceptId);
    }
}

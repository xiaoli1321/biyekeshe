package com.learnplatform.service;

import com.learnplatform.entity.Chapter;
import com.learnplatform.repository.ProgressRepository;
import com.learnplatform.repository.ChapterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 章节业务服务
 */
@Service
@Transactional
public class ChapterService {

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private ProgressRepository progressRepository;

    /**
     * 创建章节
     */
    public Chapter createChapter(Chapter chapter) {
        return chapterRepository.save(chapter);
    }

    /**
     * 更新章节
     */
    public Chapter updateChapter(Chapter chapter) {
        return chapterRepository.save(chapter);
    }

    /**
     * 创建章节（带自动排序）
     */
    public Chapter createChapterWithOrder(Chapter chapter) {
        // 如果没有指定顺序，自动计算下一个顺序
        if (chapter.getOrderIndex() == null || chapter.getOrderIndex() < 0) {
            String courseId = chapter.getCourse().getId();
            List<Chapter> existingChapters = getChaptersByCourseOrdered(courseId);
            chapter.setOrderIndex(existingChapters.size());
        }
        return chapterRepository.save(chapter);
    }

    /**
     * 根据ID查找章节
     */
    public Optional<Chapter> findChapterById(String id) {
        return chapterRepository.findById(id);
    }

    /**
     * 根据课程查找章节（按顺序排序）
     */
    public List<Chapter> getChaptersByCourseOrdered(String courseId) {
        return chapterRepository.findByCourse_IdOrderByOrderIndex(courseId);
    }

    /**
     * 统计课程章节数量
     */
    public long countChaptersByCourse(String courseId) {
        return chapterRepository.countByCourse_Id(courseId);
    }

    /**
     * 获取下一章
     */
    public Chapter getNextChapter(String courseId, Integer orderIndex) {
        List<Chapter> nextChapters = chapterRepository
                .findByCourse_IdAndOrderIndexGreaterThanOrderByOrderIndex(courseId, orderIndex);
        return nextChapters.isEmpty() ? null : nextChapters.get(0);
    }

    /**
     * 获取上一章
     */
    public Chapter getPreviousChapter(String courseId, Integer orderIndex) {
        Optional<Chapter> prevChapter = chapterRepository
                .findByCourse_IdAndOrderIndexLessThanOrderByOrderIndexDesc(courseId, orderIndex);
        return prevChapter.orElse(null);
    }

    /**
     * 获取第一章节
     */
    public Chapter getFirstChapter(String courseId) {
        List<Chapter> chapters = chapterRepository.findByCourse_IdOrderByOrderIndexAsc(courseId,
                org.springframework.data.domain.PageRequest.of(0, 1));
        return chapters.isEmpty() ? null : chapters.get(0);
    }

    /**
     * 获取最后一章节
     */
    public Chapter getLastChapter(String courseId) {
        List<Chapter> chapters = chapterRepository.findByCourse_IdOrderByOrderIndexDesc(courseId,
                org.springframework.data.domain.PageRequest.of(0, 1));
        return chapters.isEmpty() ? null : chapters.get(0);
    }

    /**
     * 删除章节
     */
    public void deleteChapter(String id) {
        Chapter chapter = findChapterById(id)
                .orElseThrow(() -> new IllegalArgumentException("章节未找到"));

        String courseId = chapter.getCourse() != null ? chapter.getCourse().getId() : null;
        conceptService.deleteConceptsByChapter(id);
        progressRepository.deleteByChapter_Id(id);
        chapterRepository.deleteById(id);

        if (courseId != null) {
            reorderChapters(courseId);
        }
    }

    public void deleteChaptersByCourse(String courseId) {
        getChaptersByCourseOrdered(courseId).stream()
                .map(Chapter::getId)
                .toList()
                .forEach(id -> {
                    conceptService.deleteConceptsByChapter(id);
                    progressRepository.deleteByChapter_Id(id);
                    chapterRepository.deleteById(id);
                });
    }

    /**
     * 排序章节（在删除或移动章节后重新排序）
     */
    public void reorderChapters(String courseId) {
        List<Chapter> chapters = getChaptersByCourseOrdered(courseId);
        for (int i = 0; i < chapters.size(); i++) {
            Chapter chapter = chapters.get(i);
            if (!chapter.getOrderIndex().equals(i)) {
                chapter.setOrderIndex(i);
                chapterRepository.save(chapter);
            }
        }
    }

    /**
     * 移动章节
     */
    public Chapter moveChapterUp(String chapterId) {
        Chapter chapter = findChapterById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("章节未找到"));

        String courseId = chapter.getCourse().getId();
        List<Chapter> chapters = getChaptersByCourseOrdered(courseId);
        int currentIndex = chapters.indexOf(chapter);

        if (currentIndex > 0) {
            Chapter previousChapter = chapters.get(currentIndex - 1);

            // 交换顺序索引
            int tempOrder = chapter.getOrderIndex();
            chapter.setOrderIndex(previousChapter.getOrderIndex());
            previousChapter.setOrderIndex(tempOrder);

            chapterRepository.save(chapter);
            chapterRepository.save(previousChapter);
        }

        return chapter;
    }

    /**
     * 移动章节
     */
    public Chapter moveChapterDown(String chapterId) {
        Chapter chapter = findChapterById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("章节未找到"));

        String courseId = chapter.getCourse().getId();
        List<Chapter> chapters = getChaptersByCourseOrdered(courseId);
        int currentIndex = chapters.indexOf(chapter);

        if (currentIndex < chapters.size() - 1) {
            Chapter nextChapter = chapters.get(currentIndex + 1);

            // 交换顺序索引
            int tempOrder = chapter.getOrderIndex();
            chapter.setOrderIndex(nextChapter.getOrderIndex());
            nextChapter.setOrderIndex(tempOrder);

            chapterRepository.save(chapter);
            chapterRepository.save(nextChapter);
        }

        return chapter;
    }

    /**
     * 获取章节知识点
     */
    public List<String> getChapterConcepts(String chapterId) {
        return List.of("JavaScript基础", "变量和函数", "DOM操作");
    }
}

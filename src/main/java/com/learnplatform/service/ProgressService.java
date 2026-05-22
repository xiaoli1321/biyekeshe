package com.learnplatform.service;

import com.learnplatform.dto.response.ChapterProgressDto;
import com.learnplatform.dto.response.CourseProgressDto;
import com.learnplatform.entity.Progress;
import com.learnplatform.entity.Chapter;
import com.learnplatform.entity.Course;
import com.learnplatform.entity.User;
import com.learnplatform.repository.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 学习进度业务服务
 */
@Service
@Transactional
public class ProgressService {

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private ChapterService chapterService;

    /**
     * 开始学习章节
     */
    public Progress startChapter(String userId, String courseId, String chapterId) {
        Progress progress = progressRepository.findByUser_IdAndChapter_Id(userId, chapterId);

        if (progress == null) {
            progress = new Progress();
            progress.setUser(new com.learnplatform.entity.User() {{ setId(userId); }});
            progress.setCourse(new com.learnplatform.entity.Course() {{ setId(courseId); }});
            progress.setChapter(new com.learnplatform.entity.Chapter() {{ setId(chapterId); }});
            progress.setStatus(Progress.ProgressStatus.IN_PROGRESS);
            progress.setStartedAt(LocalDateTime.now());
        } else {
            progress.setStatus(Progress.ProgressStatus.IN_PROGRESS);
        }

        return progressRepository.save(progress);
    }

    /**
     * 完成章节
     */
    public Progress completeChapter(String userId, String courseId, String chapterId, double score, Integer studyMinutes) {
        Progress progress = progressRepository.findByUser_IdAndChapter_Id(userId, chapterId);

        if (progress == null) {
            progress = new Progress();
            progress.setUser(new com.learnplatform.entity.User() {{ setId(userId); }});
            progress.setCourse(new com.learnplatform.entity.Course() {{ setId(courseId); }});
            progress.setChapter(new com.learnplatform.entity.Chapter() {{ setId(chapterId); }});
            progress.setStartedAt(LocalDateTime.now());
        }

        progress.setStatus(Progress.ProgressStatus.COMPLETED);
        progress.setCompletedAt(LocalDateTime.now());
        progress.setScore(score);
        progress.setStudyDurationMinutes(studyMinutes);

        return progressRepository.save(progress);
    }

    /**
     * 标记为已掌握
     */
    public Progress markAsMastered(String userId, String courseId, String chapterId) {
        Progress progress = progressRepository.findByUser_IdAndChapter_Id(userId, chapterId);

        if (progress == null) {
            throw new IllegalArgumentException("学习记录未找到");
        }

        progress.setStatus(Progress.ProgressStatus.MASTERED);
        progress.setScore(100.0);

        return progressRepository.save(progress);
    }

    /**
     * 查找用户的学习进度
     */
    public List<Progress> getUserProgress(String userId) {
        return progressRepository.findByUser_Id(userId);
    }

    /**
     * 查找用户特定课程的学习进度
     */
    public List<Progress> getUserCourseProgress(String userId, String courseId) {
        return progressRepository.findByUser_IdAndCourse_Id(userId, courseId);
    }

    /**
     * 查找特定章节的学习进度
     */
    public Progress getChapterProgress(String userId, String chapterId) {
        return progressRepository.findByUser_IdAndChapter_Id(userId, chapterId);
    }

    /**
     * 检查章节是否完成
     */
    public boolean isChapterCompleted(String userId, String chapterId) {
        return progressRepository.existsByUserIdAndChapterIdAndStatusIn(
                userId,
                chapterId,
                List.of(Progress.ProgressStatus.COMPLETED, Progress.ProgressStatus.MASTERED)
        );
    }

    /**
     * 计算课程完成率
     */
    public double calculateCourseCompletionRate(String userId, String courseId) {
        List<Double> result = progressRepository.calculateCompletionRateByUserAndCourse(
                userId,
                courseId,
                List.of(Progress.ProgressStatus.COMPLETED, Progress.ProgressStatus.MASTERED)
        );
        return result != null && !result.isEmpty() ? result.get(0) : 0.0;
    }

    /**
     * 获取用户已完成章节数量
     */
    public long getCompletedChapterCount(String userId, String courseId) {
        return progressRepository.countCompletedChaptersByUserAndCourse(
                userId,
                courseId,
                List.of(Progress.ProgressStatus.COMPLETED, Progress.ProgressStatus.MASTERED)
        );
    }

    /**
     * 获取用户的最近学习记录
     */
    public List<Progress> getRecentProgress(String userId, int limit) {
        return progressRepository.findRecentProgressByUserId(userId,
                org.springframework.data.domain.PageRequest.of(0, limit));
    }

    /**
     * 获取用户正在学习的内容
     */
    public List<Progress> getInProgressChapters(String userId) {
        return progressRepository.findInProgressByUserId(userId);
    }

    /**
     * 获取用户已完成课程
     */
    public List<Progress> getCompletedCourses(String userId) {
        return progressRepository.findCompletedCoursesByUserId(
                userId,
                List.of(Progress.ProgressStatus.COMPLETED, Progress.ProgressStatus.MASTERED)
        );
    }

    /**
     * 获取用户学习统计
     */
    public UserStats getUserLearningStats(String userId) {
        List<Long> totalMinutesList = progressRepository.totalStudyMinutesByUserId(userId);
        long totalMinutes = (totalMinutesList != null && !totalMinutesList.isEmpty() && totalMinutesList.get(0) != null) ? totalMinutesList.get(0) : 0;
        
        List<Double> avgScoreList = progressRepository.findAverageScoreByUserId(userId);
        double avgScore = (avgScoreList != null && !avgScoreList.isEmpty() && avgScoreList.get(0) != null) ? avgScoreList.get(0) : 0.0;
        
        List<Double> maxScoreList = progressRepository.findMaxScoreByUserId(userId);
        double maxScore = (maxScoreList != null && !maxScoreList.isEmpty() && maxScoreList.get(0) != null) ? maxScoreList.get(0) : 0.0;

        return new UserStats(totalMinutes, avgScore, maxScore);
    }

    /**
     * 用户统计信息记录类
     */
    public record UserStats(long totalMinutes, double avgScore, double maxScore) {}

    /**
     * 更新学习笔记
     */
    public Progress updateNotes(String userId, String chapterId, String notes) {
        Progress progress = progressRepository.findByUser_IdAndChapter_Id(userId, chapterId);

        if (progress == null) {
            throw new IllegalArgumentException("学习记录未找到");
        }

        progress.setNotes(notes);
        return progressRepository.save(progress);
    }

    /**
     * 更新学习进度
     */
    public Progress updateProgress(String userId, String chapterId, boolean completed, int studyMinutes) {
        Progress.ProgressStatus targetStatus = completed
                ? Progress.ProgressStatus.COMPLETED
                : Progress.ProgressStatus.IN_PROGRESS;
        return updateProgress(userId, chapterId, targetStatus, studyMinutes);
    }

    public Progress updateProgress(String userId, String chapterId, Progress.ProgressStatus targetStatus, int studyMinutes) {
        Progress progress = progressRepository.findByUser_IdAndChapter_Id(userId, chapterId);
        if (progress == null) {
            Chapter chapter = chapterService.findChapterById(chapterId)
                    .orElseThrow(() -> new IllegalArgumentException("章节未找到"));

            User user = new User();
            user.setId(userId);

            Course course = chapter.getCourse();

            progress = new Progress();
            progress.setUser(user);
            progress.setCourse(course);
            progress.setChapter(chapter);
            progress.setStartedAt(LocalDateTime.now());
            progress.setStatus(Progress.ProgressStatus.IN_PROGRESS);
        }

        Progress.ProgressStatus effectiveStatus = targetStatus != null
                ? targetStatus
                : Progress.ProgressStatus.IN_PROGRESS;

        if (effectiveStatus == Progress.ProgressStatus.NOT_STARTED) {
            progress.setStatus(Progress.ProgressStatus.NOT_STARTED);
            progress.setCompletedAt(null);
            progress.setScore(0.0);
        } else if (effectiveStatus == Progress.ProgressStatus.IN_PROGRESS) {
            progress.setStatus(Progress.ProgressStatus.IN_PROGRESS);
            progress.setCompletedAt(null);
        } else if (effectiveStatus == Progress.ProgressStatus.COMPLETED) {
            progress.setStatus(Progress.ProgressStatus.COMPLETED);
            progress.setCompletedAt(LocalDateTime.now());
        } else if (effectiveStatus == Progress.ProgressStatus.MASTERED) {
            progress.setStatus(Progress.ProgressStatus.MASTERED);
            progress.setCompletedAt(LocalDateTime.now());
            progress.setScore(100.0);
        } else {
            progress.setStatus(effectiveStatus);
        }

        int safeMinutes = Math.max(studyMinutes, 0);
        Integer existingMinutes = progress.getStudyDurationMinutes();
        progress.setStudyDurationMinutes((existingMinutes != null ? existingMinutes : 0) + safeMinutes);
        return progressRepository.save(progress);
    }

    /**
     * 获取课程学习进度汇总
     */
    public CourseProgressDto getCourseProgressSummary(String userId, String courseId) {
        long totalChapters = chapterService.countChaptersByCourse(courseId);
        List<Progress> progressList = progressRepository.findByUser_IdAndCourse_Id(userId, courseId);

        List<String> completedChapterIds = new ArrayList<>();
        List<String> inProgressChapterIds = new ArrayList<>();

        if (progressList != null) {
            for (Progress progress : progressList) {
                if (progress.getChapter() == null || progress.getChapter().getId() == null) {
                    continue;
                }
                String chapterId = progress.getChapter().getId();
                if (progress.isCompleted()) {
                    completedChapterIds.add(chapterId);
                } else if (progress.getStatus() == Progress.ProgressStatus.IN_PROGRESS) {
                    inProgressChapterIds.add(chapterId);
                }
            }
        }

        long completedChapters = completedChapterIds.size();
        double completionRate = totalChapters == 0 ? 0.0 : (completedChapters * 100.0 / totalChapters);

        return new CourseProgressDto(
                courseId,
                totalChapters,
                completedChapters,
                completionRate,
                completedChapterIds,
                inProgressChapterIds
        );
    }

    /**
     * 获取章节学习进度
     */
    public ChapterProgressDto getChapterProgressSummary(String userId, String chapterId) {
        Progress progress = progressRepository.findByUser_IdAndChapter_Id(userId, chapterId);
        if (progress == null) {
            return new ChapterProgressDto(
                    chapterId,
                    Progress.ProgressStatus.NOT_STARTED,
                    false,
                    0,
                    null
            );
        }

        return new ChapterProgressDto(
                chapterId,
                progress.getStatus(),
                progress.isCompleted(),
                progress.getStudyDurationMinutes(),
                progress.getCompletedAt()
        );
    }
}

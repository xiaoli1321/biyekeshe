package com.learnplatform.repository;

import com.learnplatform.entity.Progress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习进度数据访问层
 */
@Repository
public interface ProgressRepository extends MongoRepository<Progress, String> {

    /**
     * 根据用户查找进度
     */
    List<Progress> findByUser_Id(String userId);

    /**
     * 根据课程查找进度
     */
    List<Progress> findByCourse_Id(String courseId);

    /**
     * 根据章节查找进度
     */
    List<Progress> findByChapter_Id(String chapterId);

    /**
     * 根据用户和课程查找进度
     */
    List<Progress> findByUser_IdAndCourse_Id(String userId, String courseId);

    /**
     * 根据用户和章节查找进度
     */
    Progress findByUser_IdAndChapter_Id(String userId, String chapterId);

    /**
     * 根据用户和状态查找进度
     */
    List<Progress> findByUser_IdAndStatus(String userId, Progress.ProgressStatus status);

    /**
     * 根据课程和状态查找进度
     */
    List<Progress> findByCourse_IdAndStatus(String courseId, Progress.ProgressStatus status);

    /**
     * 检查用户是否已完成章节
     */
    @Query("{'user.$id': ?0, 'chapter.$id': ?1, 'status': {$in: ?2}}")
    boolean existsByUserIdAndChapterIdAndStatusIn(String userId, String chapterId,
                                                  List<Progress.ProgressStatus> statuses);

    /**
     * 统计用户的学习进度
     */
    @Query("{'user.$id': ?0, 'status': {$in: ?1}}")
    List<Progress> findByUserIdAndStatusIn(String userId, List<Progress.ProgressStatus> statuses);

    /**
     * 统计用户课程的完成率
     */
    @Query("{'user.$id': ?0, 'course.$id': ?1, 'status': {$in: ?2}}")
    long countCompletedChaptersByUserAndCourse(String userId, String courseId,
                                             List<Progress.ProgressStatus> completedStatuses);

    /**
     * 统计课程的章节总数
     */
    @Query("{'user.$id': ?0, 'course.$id': ?1}")
    long countTotalChaptersByUserAndCourse(String userId, String courseId);

    /**
     * 计算用户课程的完成率
     */
    @Aggregation(pipeline = {
        "{ $match: { 'user.$id': ?0, 'course.$id': ?1 } }",
        "{ $group: { _id: null, totalCount: { $sum: 1 }, completedCount: { $sum: { $cond: [{ $in: ['$status', ?2] }, 1, 0] } } } }",
        "{ $project: { completionRate: { $cond: [{ $gt: ['$totalCount', 0] }, { $multiply: [{ $divide: ['$completedCount', '$totalCount'] }, 100] }, 0] } } }"
    })
    List<Double> calculateCompletionRateByUserAndCourse(String userId, String courseId,
                                                      List<Progress.ProgressStatus> completedStatuses);

    /**
     * 查找用户最近学习的章节
     */
    @Query(sort = "{'updatedAt': -1}", value = "{'user.$id': ?0}")
    List<Progress> findRecentProgressByUserId(String userId, Pageable pageable);

    /**
     * 按状态统计进度数量
     */
    @Aggregation(pipeline = {
        "{ $match: { 'user.$id': ?0 } }",
        "{ $group: { _id: '$status', count: { $sum: 1 } } }"
    })
    List<Object[]> countByStatus(String userId);

    /**
     * 按课程统计进度数量
     */
    @Aggregation(pipeline = {
        "{ $group: { _id: '$course.$id', count: { $sum: 1 } } }"
    })
    List<Object[]> countByCourse();

    /**
     * 查找用户正在进行的学习
     */
    @Query(value = "{'user.$id': ?0, 'status': 'IN_PROGRESS'}", sort = "{'updatedAt': -1}")
    List<Progress> findInProgressByUserId(String userId);

    /**
     * 查找用户已完成的课程
     */
    @Query("{'user.$id': ?0, 'status': {$in: ?1}}")
    List<Progress> findCompletedCoursesByUserId(String userId,
                                             List<Progress.ProgressStatus> completedStatuses);

    /**
     * 查找用户未完成的课程
     */
    @Query("{'user.$id': ?0, 'status': {$nin: ?1}}")
    List<Progress> findIncompleteCoursesByUserId(String userId,
                                              List<Progress.ProgressStatus> completedStatuses);

    /**
     * 按学习时长统计
     */
    @Aggregation(pipeline = {
        "{ $match: { 'user.$id': ?0 } }",
        "{ $group: { _id: null, totalMinutes: { $sum: '$studyDurationMinutes' } } }"
    })
    List<Long> totalStudyMinutesByUserId(String userId);

    /**
     * 按时间段统计学习
     */
    @Aggregation(pipeline = {
        "{ $match: { 'user.$id': ?0, 'completedAt': { $gte: ?1, $lte: ?2 } } }",
        "{ $group: { _id: null, totalMinutes: { $sum: '$studyDurationMinutes' } } }"
    })
    List<Long> sumStudyMinutesByUserIdAndDateRange(String userId,
                                           LocalDateTime startDate,
                                           LocalDateTime endDate);

    /**
     * 查找用户最高得分
     */
    @Aggregation(pipeline = {
        "{ $match: { 'user.$id': ?0 } }",
        "{ $group: { _id: null, maxScore: { $max: '$score' } } }"
    })
    List<Double> findMaxScoreByUserId(String userId);

    /**
     * 查找用户平均得分
     */
    @Aggregation(pipeline = {
        "{ $match: { 'user.$id': ?0 } }",
        "{ $group: { _id: null, avgScore: { $avg: '$score' } } }"
    })
    List<Double> findAverageScoreByUserId(String userId);
}

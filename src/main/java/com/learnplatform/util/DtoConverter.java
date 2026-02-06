package com.learnplatform.util;

import com.learnplatform.dto.ApiResponse;
import com.learnplatform.dto.PageResponse;
import com.learnplatform.dto.response.CourseDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO转换工具类
 */
public class DtoConverter {

    private DtoConverter() {
        // 私有构造器，防止实例化
    }

    /**
     * 将实体列表转换为DTO列表
     */
    public static <T, D> List<D> convertList(List<T> source, java.util.function.Function<T, D> converter) {
        if (source == null) {
            return null;
        }
        return source.stream()
                .map(converter)
                .collect(Collectors.toList());
    }

    /**
     * 将Page对象转换为PageResponse
     */
    public static <T, D> PageResponse<D> convertPage(Page<T> source,
                                                      java.util.function.Function<T, D> converter) {
        if (source == null) {
            return null;
        }

        List<D> content = convertList(source.getContent(), converter);

        return new PageResponse<>(
                content,
                source.getTotalElements(),
                source.getTotalPages(),
                source.getNumber(),
                source.getSize()
        );
    }

    /**
     * 为课程列表添加推荐标志的辅助方法
     */
    public static List<CourseDto> markRecommendedCourses(List<CourseDto> courses,
                                                         List<String> recommendedIds) {
        if (courses == null || recommendedIds == null) {
            return courses;
        }

        return courses.stream()
                .map(course -> {
                    if (recommendedIds.contains(course.getId())) {
                        // 可以为推荐课程添加额外标记
                        // course.setRecommended(true);
                    }
                    return course;
                })
                .collect(Collectors.toList());
    }

    /**
     * 构建错误响应
     */
    public static <T> ApiResponse<T> errorResponse(String message) {
        return ApiResponse.error(message);
    }

    /**
     * 构建错误响应
     */
    public static <T> ApiResponse<T> errorResponse(String message, String errorCode) {
        return ApiResponse.error(message, errorCode);
    }

    /**
     * 构建成功响应
     */
    public static <T> ApiResponse<T> successResponse(T data) {
        return ApiResponse.success(data);
    }

    /**
     * 构建成功响应（带消息）
     */
    public static <T> ApiResponse<T> successResponse(T data, String message) {
        return ApiResponse.success(data, message);
    }
}
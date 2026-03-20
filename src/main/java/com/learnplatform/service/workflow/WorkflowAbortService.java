package com.learnplatform.service.workflow;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 工作流中断管理服务
 * 用内存 ConcurrentHashMap 保存运行中任务的中止标志
 * 等价于原 JS 中的 AbortController 机制
 */
@Service
public class WorkflowAbortService {

    /** streamingId → 中止标志 */
    private final ConcurrentHashMap<String, AtomicBoolean> abortFlags = new ConcurrentHashMap<>();

    /**
     * 注册一个任务的中止标志（任务开始时调用）
     *
     * @param streamingId 任务唯一 ID
     * @return 中止标志 AtomicBoolean，streaming 循环持有引用判断是否需要停止
     */
    public AtomicBoolean register(String streamingId) {
        AtomicBoolean flag = new AtomicBoolean(false);
        abortFlags.put(streamingId, flag);
        return flag;
    }

    /**
     * 中止指定任务
     *
     * @param streamingId 任务唯一 ID
     * @return true 表示成功找到并中止；false 表示未找到该任务
     */
    public boolean abort(String streamingId) {
        AtomicBoolean flag = abortFlags.get(streamingId);
        if (flag != null) {
            flag.set(true);
            return true;
        }
        return false;
    }

    /**
     * 任务完成后清理标志（避免内存泄漏）
     */
    public void cleanup(String streamingId) {
        abortFlags.remove(streamingId);
    }

    /**
     * 查询任务是否已被中止
     */
    public boolean isAborted(String streamingId) {
        AtomicBoolean flag = abortFlags.get(streamingId);
        return flag != null && flag.get();
    }
}

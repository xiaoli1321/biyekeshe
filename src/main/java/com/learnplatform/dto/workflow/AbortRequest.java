package com.learnplatform.dto.workflow;

/**
 * 中断工作流请求 DTO
 */
public class AbortRequest {

    private String streamingId;

    public String getStreamingId() { return streamingId; }
    public void setStreamingId(String streamingId) { this.streamingId = streamingId; }
}

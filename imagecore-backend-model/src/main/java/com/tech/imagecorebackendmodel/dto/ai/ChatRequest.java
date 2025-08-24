package com.tech.imagecorebackendmodel.dto.ai;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ChatRequest {

    /**
     * 用户 id
     */
    private Long userId;
    /**
     * 对话 id
     */
    private String chatId;
    /**
     * 用户提示词
     */
    private String userMessage;
    /**
     * 对话类型
     */
    private String chatType;

    /**
     * 最新的时间戳
     */
    private Timestamp lastTimestamp;
}

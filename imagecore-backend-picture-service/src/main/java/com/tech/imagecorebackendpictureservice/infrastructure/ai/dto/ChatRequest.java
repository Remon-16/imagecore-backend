package com.tech.imagecorebackendpictureservice.infrastructure.ai.dto;

import lombok.Data;

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
}

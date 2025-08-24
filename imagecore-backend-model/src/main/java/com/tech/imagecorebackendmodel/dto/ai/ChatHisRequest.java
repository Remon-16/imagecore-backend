package com.tech.imagecorebackendmodel.dto.ai;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatHisRequest {
    /**
     * 用户 id
     */
    private Long userId;
    /**
     * 对话 id
     */
    private String chatId;
    /**
     * 最后一条消息的游标
     */
    private LocalDateTime lastChatTime;
}

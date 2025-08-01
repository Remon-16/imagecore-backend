package com.tech.imagecorebackendpictureservice.infrastructure.ai.vo;

import lombok.Data;

@Data
public class ChatHisVo {

    private Integer messageId;

    private String chatId;

    private String chatContent;

    private String chatType;

    private String timestamp;
}

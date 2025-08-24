package com.tech.imagecorebackendmodel.vo.ai;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatHisVo {

    private Integer messageId;

    private String chatId;

    private String chatContent;

    private String chatType;

    private String timestamp;

    private LocalDateTime createTime;
}

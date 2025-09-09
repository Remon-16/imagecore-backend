package com.tech.imagecorebackendmodel.vo.ai;

import lombok.Data;


import java.util.Date;

@Data
public class ChatHisVo {

    private Long messageId;

    private String chatId;

    private String chatContent;

    private String chatType;

    private String timestamp;

    private Date createTime;
}

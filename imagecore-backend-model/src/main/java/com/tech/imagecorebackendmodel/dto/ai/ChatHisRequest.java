package com.tech.imagecorebackendmodel.dto.ai;

import com.tech.imagecorebackendcommon.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class ChatHisRequest  extends PageRequest implements Serializable {
    /**
     * 用户 id
     */
    private Long userId;
    /**
     * 对话 id
     */
    private String chatId;
    /**
     * 聊天类型
     */
    private String chatType;
    /**
     * 最后一条消息的游标
     */
    private Date lastChatTime;
}

package com.tech.imagecorebackendmodel.vo.ai;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatHisListVo {
    /**
     * 每条对话的历史
     */
    private List<ChatHisVo> chatHisList;
    /**
     * 对话历史 id
     */
    private String chatId;
    /**
     * 当前对话种的最新消息的游标
     */
    private LocalDateTime lastCreateTime;
}

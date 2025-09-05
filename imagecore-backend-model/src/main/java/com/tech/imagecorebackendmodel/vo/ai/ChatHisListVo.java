package com.tech.imagecorebackendmodel.vo.ai;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class ChatHisListVo {
    /**
     * 每条对话的历史
     */
    private Page<ChatHisVo> chatHisList;
    /**
     * 对话历史 id
     */
    private String chatId;
    /**
     * 当前对话种的最新消息的游标
     */
    private Date lastCreateTime;
}

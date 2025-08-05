package com.tech.imagecorebackendmodel.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户和AI聊天的关联表
 * @TableName user_ai_chat
 */
@TableName(value ="user_ai_chat")
@Data
public class UserAiChat {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * chat id
     */
    private String conversationId;
}
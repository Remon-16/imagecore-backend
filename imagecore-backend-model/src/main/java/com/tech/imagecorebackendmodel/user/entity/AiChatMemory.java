package com.tech.imagecorebackendmodel.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 
 * @TableName ai_chat_memory
 */
@TableName(value ="ai_chat_memory")
@Data
public class AiChatMemory {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String conversation_id;

    /**
     * 
     */
    private String content;

    /**
     * 
     */
    private String type;

    /**
     * 
     */
    private Date timestamp;
}
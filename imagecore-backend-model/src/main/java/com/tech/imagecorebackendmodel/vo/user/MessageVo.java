package com.tech.imagecorebackendmodel.vo.user;

import com.tech.imagecorebackendmodel.user.entity.Message;
import com.tech.imagecorebackendmodel.vo.picture.PictureCommentVo;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

@Data
public class MessageVo implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 消息状态
     */
    private String messageState;

    /**
     * 发送者 id
     */
    private Long senderId;

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 评论 id
     */
    private Long commentId;

    /**
     * 评论
     */
    private PictureCommentVo pictureCommentVo;


    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;

    public static MessageVo objToVo(Message message){
        if(message == null){
            return null;
        }
        MessageVo messageVo = new MessageVo();
        BeanUtils.copyProperties(message,messageVo);
        return messageVo;
    }
}

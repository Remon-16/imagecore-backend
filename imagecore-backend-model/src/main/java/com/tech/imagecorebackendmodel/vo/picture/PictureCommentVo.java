package com.tech.imagecorebackendmodel.vo.picture;

import com.tech.imagecorebackendmodel.picture.entity.PictureComment;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Data
public class PictureCommentVo {
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 目标 id 为空代表是直接评论在图片上，不为空说明是多级评论
     */
    private Long targetId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;

    public static PictureCommentVo objToVo(PictureComment pictureComment) {
        PictureCommentVo pictureCommentVo = new PictureCommentVo();
        BeanUtils.copyProperties(pictureComment, pictureCommentVo);
        return pictureCommentVo;
    }
}

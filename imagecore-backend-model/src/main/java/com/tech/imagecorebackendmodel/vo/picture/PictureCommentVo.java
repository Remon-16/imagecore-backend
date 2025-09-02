package com.tech.imagecorebackendmodel.vo.picture;

import com.tech.imagecorebackendmodel.picture.entity.PictureComment;
import com.tech.imagecorebackendmodel.vo.user.UserVO;
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
     * 二级目标 id 为空代表是二级评论，不为空说明是三级评论
     */
    private Long secondTargetId;

    /**
     * 目标用户的Id
     */
    private Long targetUserId;

    /**
     * 目标用户的昵称
     */
    private String targetUserName;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 用户信息
     */
    private UserVO user;

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

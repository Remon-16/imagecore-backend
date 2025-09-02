package com.tech.imagecorebackendmodel.vo.picture;

import com.tech.imagecorebackendmodel.vo.user.UserVO;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class PictureCommentRootVo {
    /**
     * id
     */
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
     * 评论内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 用户信息
     */
    private UserVO user;

    /**
     * 子评论
     */
    List<PictureCommentVo> pictureCommentVos;
}

package com.tech.imagecorebackendmodel.dto.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserUpdateInfoRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 会员到期时间
     */
    private Date vipExpiry;
    /**
     * 会员类型
     */
    private String vipType;

    private static final long serialVersionUID = 1L;
}

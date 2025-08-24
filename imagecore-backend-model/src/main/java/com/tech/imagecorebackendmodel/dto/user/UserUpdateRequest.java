package com.tech.imagecorebackendmodel.dto.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 更新用户请求
 */
@Data
public class UserUpdateRequest implements Serializable {

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

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
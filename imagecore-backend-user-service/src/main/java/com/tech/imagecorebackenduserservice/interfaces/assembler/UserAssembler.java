package com.tech.imagecorebackenduserservice.interfaces.assembler;

import com.tech.imagecorebackendmodel.dto.user.UserAddRequest;
import com.tech.imagecorebackendmodel.dto.user.UserUpdateInfoRequest;
import com.tech.imagecorebackendmodel.dto.user.UserUpdateRequest;
import com.tech.imagecorebackendmodel.user.entity.User;
import org.springframework.beans.BeanUtils;

/**
 * 用户对象转换
 */
public class UserAssembler {

    public static User toUserEntity(UserAddRequest request) {
        User user = new User();
        BeanUtils.copyProperties(request, user);
        return user;
    }

    public static User toUserEntity(UserUpdateRequest request) {
        User user = new User();
        BeanUtils.copyProperties(request, user);
        return user;
    }

    public static User toUserEntity(UserUpdateInfoRequest request) {
        User user = new User();
        BeanUtils.copyProperties(request, user);
        return user;
    }
}
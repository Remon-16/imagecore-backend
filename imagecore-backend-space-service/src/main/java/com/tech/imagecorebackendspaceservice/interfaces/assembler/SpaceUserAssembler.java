package com.tech.imagecorebackendspaceservice.interfaces.assembler;

import com.tech.imagecorebackendmodel.dto.spaceuser.SpaceUserAddRequest;
import com.tech.imagecorebackendmodel.dto.spaceuser.SpaceUserEditRequest;
import com.tech.imagecorebackendmodel.space.entity.SpaceUser;
import org.springframework.beans.BeanUtils;

/**
 * 空间用户对象转换
 */
public class SpaceUserAssembler {

    public static SpaceUser toSpaceUserEntity(SpaceUserAddRequest request) {
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(request, spaceUser);
        return spaceUser;
    }

    public static SpaceUser toSpaceUserEntity(SpaceUserEditRequest request) {
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(request, spaceUser);
        return spaceUser;
    }
}
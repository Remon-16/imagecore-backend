package com.tech.imagecorebackendspaceservice.domain.space.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tech.imagecorebackendmodel.dto.spaceuser.SpaceUserQueryRequest;
import com.tech.imagecorebackendmodel.space.entity.SpaceUser;

/**
 * @author Remon
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2025-01-02 20:07:15
 */
public interface SpaceUserDomainService {

    /**
     * 获取查询对象
     *
     * @param spaceUserQueryRequest
     * @return
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);
}

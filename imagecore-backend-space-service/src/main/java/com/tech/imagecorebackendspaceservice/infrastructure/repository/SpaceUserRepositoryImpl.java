package com.tech.imagecorebackendspaceservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.imagecorebackendmodel.space.entity.SpaceUser;
import com.tech.imagecorebackendspaceservice.domain.space.repository.SpaceUserRepository;
import com.tech.imagecorebackendspaceservice.infrastructure.mapper.SpaceUserMapper;
import org.springframework.stereotype.Service;

/**
 * 空间用户仓储实现
 */
@Service
public class SpaceUserRepositoryImpl extends ServiceImpl<SpaceUserMapper, SpaceUser> implements SpaceUserRepository {
}
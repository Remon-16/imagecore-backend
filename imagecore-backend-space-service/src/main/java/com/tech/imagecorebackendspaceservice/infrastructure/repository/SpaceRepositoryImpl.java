package com.tech.imagecorebackendspaceservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.imagecorebackendmodel.space.entity.Space;
import com.tech.imagecorebackendspaceservice.domain.space.repository.SpaceRepository;
import com.tech.imagecorebackendspaceservice.infrastructure.mapper.SpaceMapper;
import org.springframework.stereotype.Service;

/**
 * 空间仓储实现
 */
@Service
public class SpaceRepositoryImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceRepository {
}
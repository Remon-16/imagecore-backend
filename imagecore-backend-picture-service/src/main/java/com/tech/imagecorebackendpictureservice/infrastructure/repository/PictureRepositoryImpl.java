package com.tech.imagecorebackendpictureservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.imagecorebackendmodel.picture.entity.Picture;
import com.tech.imagecorebackendpictureservice.domain.picture.repository.PictureRepository;
import com.tech.imagecorebackendpictureservice.infrastructure.mapper.PictureMapper;
import org.springframework.stereotype.Service;

/**
 * 图片仓储实现
 */
@Service
public class PictureRepositoryImpl extends ServiceImpl<PictureMapper, Picture> implements PictureRepository {
}
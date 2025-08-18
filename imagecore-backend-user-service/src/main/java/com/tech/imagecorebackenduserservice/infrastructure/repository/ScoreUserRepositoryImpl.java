package com.tech.imagecorebackenduserservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.imagecorebackendmodel.user.entity.ScoreUser;
import com.tech.imagecorebackenduserservice.domain.user.repository.ScoreUserRepository;
import com.tech.imagecorebackenduserservice.infrastructure.mapper.ScoreUserMapper;
import org.springframework.stereotype.Service;

@Service
public class ScoreUserRepositoryImpl extends ServiceImpl<ScoreUserMapper, ScoreUser> implements ScoreUserRepository {
}

package com.tech.imagecorebackenduserservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackenduserservice.domain.user.repository.UserRepository;
import com.tech.imagecorebackenduserservice.infrastructure.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
 * 用户仓储实现
 */
@Service
public class UserRepositoryImpl extends ServiceImpl<UserMapper, User> implements UserRepository {
}
package com.tech.imagecorebackenduserservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.imagecorebackendmodel.user.entity.UserAiChat;
import com.tech.imagecorebackenduserservice.domain.user.repository.UserAiChatRepository;
import com.tech.imagecorebackenduserservice.infrastructure.mapper.UserAiChatMapper;
import org.springframework.stereotype.Service;

@Service
public class UserAiChatRepositoryImpl extends ServiceImpl<UserAiChatMapper, UserAiChat> implements UserAiChatRepository {
}

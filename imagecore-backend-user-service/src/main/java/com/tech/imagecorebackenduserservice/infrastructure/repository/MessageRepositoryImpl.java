package com.tech.imagecorebackenduserservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.imagecorebackendmodel.user.entity.Message;
import com.tech.imagecorebackenduserservice.domain.user.repository.MessageRepository;
import com.tech.imagecorebackenduserservice.infrastructure.mapper.MessageMapper;
import org.springframework.stereotype.Service;

@Service
public class MessageRepositoryImpl extends ServiceImpl<MessageMapper, Message> implements MessageRepository {
}

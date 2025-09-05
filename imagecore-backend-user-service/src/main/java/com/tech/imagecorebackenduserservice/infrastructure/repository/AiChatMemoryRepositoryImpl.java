package com.tech.imagecorebackenduserservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.imagecorebackendmodel.user.entity.AiChatMemory;
import com.tech.imagecorebackenduserservice.domain.user.repository.AiChatMemoryRepository;
import com.tech.imagecorebackenduserservice.infrastructure.mapper.AiChatMemoryMapper;
import org.springframework.stereotype.Service;

/**
* @author Remon
* @description 针对表【ai_chat_memory】的数据库操作Service实现
* @createDate 2025-09-05 19:07:25
*/
@Service
public class AiChatMemoryRepositoryImpl extends ServiceImpl<AiChatMemoryMapper, AiChatMemory>
    implements AiChatMemoryRepository {

}





package com.tech.imagecorebackenduserservice.application.service.impl;


import com.tech.imagecorebackendmodel.dto.ai.ChatHisRequest;
import com.tech.imagecorebackendmodel.dto.ai.ChatRequest;
import com.tech.imagecorebackendmodel.vo.ai.ChatAllHisResponse;
import com.tech.imagecorebackendmodel.vo.ai.ChatHisResponse;
import com.tech.imagecorebackenduserservice.application.service.UserAiChatApplicationService;
import com.tech.imagecorebackenduserservice.domain.user.service.UserAiChatDomainService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class UserAiChatApplicationServiceImpl implements UserAiChatApplicationService {

    @Resource
    private UserAiChatDomainService userAiChatDomainService;

    @Override
    public Flux<String> doChatStreamService(ChatRequest chatRequest) {
        return userAiChatDomainService.doChatStreamService(chatRequest);
    }

    @Override
    public ChatAllHisResponse getALLChatHisResponse(ChatHisRequest chatHisRequest) {
        return userAiChatDomainService.getALLChatHisResponse(chatHisRequest);
    }

    @Override
    public ChatHisResponse getChatHisResponse(ChatRequest chatRequest) {
        return userAiChatDomainService.getChatHisResponse(chatRequest);
    }
}

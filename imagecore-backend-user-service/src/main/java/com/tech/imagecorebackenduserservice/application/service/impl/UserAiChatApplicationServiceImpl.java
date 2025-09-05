package com.tech.imagecorebackenduserservice.application.service.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.imagecorebackendmodel.dto.ai.ChatHisRequest;
import com.tech.imagecorebackendmodel.dto.ai.ChatRequest;
import com.tech.imagecorebackendmodel.vo.ai.ChatAllHisResponse;
import com.tech.imagecorebackendmodel.vo.ai.ChatHisListVo;
import com.tech.imagecorebackendmodel.vo.ai.ChatHisResponse;
import com.tech.imagecorebackendmodel.vo.ai.ChatHisVo;
import com.tech.imagecorebackenduserservice.application.service.UserAiChatApplicationService;
import com.tech.imagecorebackenduserservice.domain.user.service.UserAiChatDomainService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class UserAiChatApplicationServiceImpl implements UserAiChatApplicationService {

    @Resource
    private UserAiChatDomainService userAiChatDomainService;

    @Override
    public String getNewChatId(String chatType, HttpServletRequest request) {
        return userAiChatDomainService.getNewChatId(chatType, request);
    }

    @Override
    public String doAIChat(ChatRequest chatRequest) {
        return userAiChatDomainService.doAIChatService(chatRequest);
    }

    @Override
    public Flux<ServerSentEvent<String>> doChatStreamService(ChatRequest chatRequest) {
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

    @Override
    public List<ChatHisListVo> getUserAllChatHis(ChatHisRequest chatHisRequest) {
        return userAiChatDomainService.getUserAllChatHis(chatHisRequest);
    }

    @Override
    public Page<ChatHisVo> getChatHisVo(ChatHisRequest chatHisRequest) {
        return userAiChatDomainService.getChatHisVo(chatHisRequest);
    }
}

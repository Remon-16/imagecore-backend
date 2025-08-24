package com.tech.imagecorebackenduserservice.domain.user.service.impl;


import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendcommon.exception.ThrowUtils;
import com.tech.imagecorebackendmodel.dto.ai.ChatHisRequest;
import com.tech.imagecorebackendmodel.dto.ai.ChatRequest;
import com.tech.imagecorebackendmodel.user.entity.UserAIChatHis;
import com.tech.imagecorebackendmodel.vo.ai.ChatAllHisResponse;
import com.tech.imagecorebackendmodel.vo.ai.ChatHisListVo;
import com.tech.imagecorebackendmodel.vo.ai.ChatHisResponse;
import com.tech.imagecorebackendmodel.vo.ai.ChatHisVo;
import com.tech.imagecorebackenduserservice.domain.user.service.UserAiChatDomainService;
import com.tech.imagecorebackenduserservice.infrastructure.ai.service.ChatDomainService;
import com.tech.imagecorebackenduserservice.infrastructure.ai.service.impl.ChatDomainServiceImpl;
import com.tech.imagecorebackenduserservice.infrastructure.mapper.UserAiChatMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserAiChatDomainServiceImpl implements UserAiChatDomainService {
    @Resource
    private ChatDomainService chatDomainService;

    @Resource
    private UserAiChatMapper userAiChatMapper;

    @Override
    public Flux<String> doChatStreamService(ChatRequest chatRequest) {
        return chatDomainService.doChatStreamService(chatRequest);
    }

    @Override
    public ChatAllHisResponse getALLChatHisResponse(ChatHisRequest chatHisRequest) {
        ThrowUtils.throwIf(chatHisRequest == null, ErrorCode.PARAMS_ERROR, "chatHisRequest 为空");
        ThrowUtils.throwIf(chatHisRequest.getUserId() == null, ErrorCode.PARAMS_ERROR, "userID 为空");

        List<UserAIChatHis> userAllAIChatHisList = userAiChatMapper.queryALLChatHistoryByUser(chatHisRequest.getUserId());
        ChatAllHisResponse chatAllHisResponse = new ChatAllHisResponse();
        List<ChatHisListVo> chatHisList = new ArrayList<>();
        String cruChatId = null;
        ChatHisListVo chatHisListVo = null;
        for(UserAIChatHis userAIChatHis : userAllAIChatHisList) {
            String chatId = userAIChatHis.getChatId();
            if(!chatId.equals(cruChatId)) {
                // 存上一个对话
                if(chatHisListVo != null) {
                    chatHisListVo.setLastCreateTime(userAIChatHis.getTimestamp().toLocalDateTime());
                    chatHisListVo.setChatId(chatId);
                    chatHisList.add(chatHisListVo);
                }
                // 初始化
                chatHisListVo = new ChatHisListVo();
                chatHisListVo.setChatHisList(new ArrayList<>());
                cruChatId = chatId;
            }
            ChatHisVo chatHisVo = new ChatHisVo();
            chatHisVo.setChatId(chatId);
            chatHisVo.setChatType(userAIChatHis.getChatType());
            chatHisVo.setChatContent(userAIChatHis.getChatContent());
            chatHisVo.setCreateTime(userAIChatHis.getTimestamp().toLocalDateTime());
            chatHisListVo.getChatHisList().add(chatHisVo);
        }
        if(cruChatId != null) {
            chatHisListVo.setChatId(cruChatId);
            chatHisListVo.setLastCreateTime(userAllAIChatHisList.getLast().getTimestamp().toLocalDateTime());
            chatHisList.add(chatHisListVo);
        }
        chatAllHisResponse.setChatHisList(chatHisList);
        return chatAllHisResponse;
    }

    @Override
    public ChatHisResponse getChatHisResponse(ChatRequest chatRequest) {
        ThrowUtils.throwIf(chatRequest == null, ErrorCode.PARAMS_ERROR, "chatHisRequest 为空");
        ThrowUtils.throwIf(chatRequest.getUserId() == null, ErrorCode.PARAMS_ERROR, "userId 为空");
        ThrowUtils.throwIf(chatRequest.getChatId() == null, ErrorCode.PARAMS_ERROR, "chatId 为空");

        List<UserAIChatHis> userAIChatHisList = userAiChatMapper
                .queryChatHistoryByUserAndTime(
                        chatRequest.getUserId(), chatRequest.getChatId(), chatRequest.getLastTimestamp());
        ChatHisResponse chatHisResponse = new ChatHisResponse();
        LocalDateTime lastCreateTime = userAIChatHisList.getLast().getTimestamp().toLocalDateTime();
        chatHisResponse.setLastCreateTime(lastCreateTime);
        List<ChatHisVo> chatHisList = new ArrayList<>();
        for(UserAIChatHis userAIChatHis : userAIChatHisList) {
            ChatHisVo chatHisVo = new ChatHisVo();
            chatHisVo.setChatType(userAIChatHis.getChatType());
            chatHisVo.setChatContent(userAIChatHis.getChatContent());
            chatHisVo.setCreateTime(userAIChatHis.getTimestamp().toLocalDateTime());
            chatHisList.add(chatHisVo);
        }
        chatHisResponse.setChatHisList(chatHisList);
        return chatHisResponse;
    }

}

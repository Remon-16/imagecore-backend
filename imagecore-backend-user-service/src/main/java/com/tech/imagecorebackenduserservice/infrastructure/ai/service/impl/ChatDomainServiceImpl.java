package com.tech.imagecorebackenduserservice.infrastructure.ai.service.impl;


import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackenduserservice.infrastructure.ai.app.CSChatApp;
import com.tech.imagecorebackenduserservice.infrastructure.ai.app.DrawChatApp;
import com.tech.imagecorebackendmodel.dto.ai.ChatRequest;
import com.tech.imagecorebackendmodel.ai.valueobject.ChatTypeEnum;
import com.tech.imagecorebackenduserservice.infrastructure.ai.service.ChatDomainService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatDomainServiceImpl implements ChatDomainService {
    /**
     * 客服对话
     */
    @Resource
    private CSChatApp csChatApp;
    /**
     * 绘图提示词对话
     */
    @Resource
    private DrawChatApp drawChatApp;

    @Override
    public Flux<String> doChatStreamService(ChatRequest chatRequest){

        String chatType = chatRequest.getChatType();
        ChatTypeEnum.isEnumValue(chatType);
        if (chatType.equals(ChatTypeEnum.CS.name())){
            return csChatApp.doChatStream(chatRequest.getChatId(), chatRequest.getUserMessage());
        }else if (chatType.equals(ChatTypeEnum.DRAW.name())){
            return drawChatApp.doChatStream(chatRequest.getChatId(), chatRequest.getUserMessage());
        }else{
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "聊天类型不正确");
        }
    }
}

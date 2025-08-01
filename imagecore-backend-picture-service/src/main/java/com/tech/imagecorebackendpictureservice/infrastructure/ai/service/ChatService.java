package com.tech.imagecorebackendpictureservice.infrastructure.ai.service;


import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendpictureservice.infrastructure.ai.app.CSChatApp;
import com.tech.imagecorebackendpictureservice.infrastructure.ai.app.DrawChatApp;
import com.tech.imagecorebackendpictureservice.infrastructure.ai.dto.ChatRequest;
import com.tech.imagecorebackendpictureservice.infrastructure.ai.enums.ChatTypeEnum;
import jakarta.annotation.Resource;
import reactor.core.publisher.Flux;

public class ChatService {
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

    public Flux<String> doChatStreamService(ChatRequest chatRequest){

        // TODO 判断用户是否为会员
        // TODO 如果不是，需要扣除积分，积分不足就报错
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

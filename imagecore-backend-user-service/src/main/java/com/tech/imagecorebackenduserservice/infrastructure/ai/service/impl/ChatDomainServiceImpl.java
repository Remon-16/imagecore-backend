package com.tech.imagecorebackenduserservice.infrastructure.ai.service.impl;


import cn.hutool.json.JSONUtil;
import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackenduserservice.infrastructure.ai.app.CSChatApp;
import com.tech.imagecorebackenduserservice.infrastructure.ai.app.DrawChatApp;
import com.tech.imagecorebackendmodel.dto.ai.ChatRequest;
import com.tech.imagecorebackendmodel.ai.valueobject.ChatTypeEnum;
import com.tech.imagecorebackenduserservice.infrastructure.ai.service.ChatDomainService;
import jakarta.annotation.Resource;
import opennlp.tools.util.StringUtil;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

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

    public String createChatId(){
        return UUID.randomUUID().toString();
    }

    @Override
    public String doChatService(ChatRequest chatRequest){
        if(chatRequest == null || StringUtil.isEmpty(chatRequest.getChatId())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String chatType = chatRequest.getChatType();
        ChatTypeEnum.isEnumValue(chatType);

        if (chatType.equals(ChatTypeEnum.CS.name())){
            return csChatApp.getChat(chatRequest.getChatId(), chatRequest.getUserMessage());
        }else if (chatType.equals(ChatTypeEnum.DRAW.name())){
            return  drawChatApp.getDrawChat(chatRequest.getChatId(), chatRequest.getUserMessage());
        }else{
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "聊天类型不正确");
        }
    }

    @Override
    public Flux<ServerSentEvent<String>> doChatStreamService(ChatRequest chatRequest){
        if(chatRequest == null || StringUtil.isEmpty(chatRequest.getChatId())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String chatType = chatRequest.getChatType();
        ChatTypeEnum.isEnumValue(chatType);
        Flux<String> contentFlux = null;

        if (chatType.equals(ChatTypeEnum.CS.name())){
            contentFlux =  csChatApp.doChatStream(chatRequest.getChatId(), chatRequest.getUserMessage());
        }else if (chatType.equals(ChatTypeEnum.DRAW.name())){
            contentFlux =  drawChatApp.doChatStream(chatRequest.getChatId(), chatRequest.getUserMessage());
        }else{
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "聊天类型不正确");
        }

        return contentFlux
                .map(chunk -> {
                    Map<String, String> wrapper = Map.of("d", chunk);
                    String jsonData = JSONUtil.toJsonStr(wrapper);
                    return ServerSentEvent.<String>builder()
                            .data(jsonData)
                            .build();
                })
                .concatWith(Mono.just(
                        // 发送结束事件
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()
                ));
    }
}

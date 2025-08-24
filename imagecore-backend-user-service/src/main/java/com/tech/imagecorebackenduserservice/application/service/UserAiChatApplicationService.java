package com.tech.imagecorebackenduserservice.application.service;

import com.tech.imagecorebackendmodel.dto.ai.ChatHisRequest;
import com.tech.imagecorebackendmodel.dto.ai.ChatRequest;
import com.tech.imagecorebackendmodel.vo.ai.ChatAllHisResponse;
import com.tech.imagecorebackendmodel.vo.ai.ChatHisResponse;
import reactor.core.publisher.Flux;

public interface UserAiChatApplicationService {

    /**
     * AI 聊天调用
     * @param chatRequest
     * @return
     */
    Flux<String> doChatStreamService(ChatRequest chatRequest);

    /**
     * 获取全部聊天历史记录
     * @param chatHisRequest
     * @return
     */
    ChatAllHisResponse getALLChatHisResponse(ChatHisRequest chatHisRequest);

    ChatHisResponse getChatHisResponse(ChatRequest chatRequest);
}

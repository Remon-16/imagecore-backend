package com.tech.imagecorebackenduserservice.domain.user.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.imagecorebackendmodel.dto.ai.ChatHisRequest;
import com.tech.imagecorebackendmodel.dto.ai.ChatRequest;
import com.tech.imagecorebackendmodel.vo.ai.ChatAllHisResponse;
import com.tech.imagecorebackendmodel.vo.ai.ChatHisListVo;
import com.tech.imagecorebackendmodel.vo.ai.ChatHisResponse;
import com.tech.imagecorebackendmodel.vo.ai.ChatHisVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.List;

public interface UserAiChatDomainService {

    String getNewChatId(String chatType, HttpServletRequest request);

    /**
     * AI 聊天调用
     * @param chatRequest
     * @return
     */
    String doAIChatService(ChatRequest chatRequest);

    List<ChatHisListVo> getUserAllChatHis(ChatHisRequest chatHisRequest);

    Page<ChatHisVo> getChatHisVo(ChatHisRequest chatHisRequest);
    /**
     * AI 聊天调用
     * @param chatRequest
     * @return
     */
    Flux<ServerSentEvent<String>> doChatStreamService(ChatRequest chatRequest);



    /**
     * 获取全部聊天历史记录
     * @param chatHisRequest
     * @return
     */
    ChatAllHisResponse getALLChatHisResponse(ChatHisRequest chatHisRequest);

    ChatHisResponse getChatHisResponse(ChatRequest chatRequest);
}

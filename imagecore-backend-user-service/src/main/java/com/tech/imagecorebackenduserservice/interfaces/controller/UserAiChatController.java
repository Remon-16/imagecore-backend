package com.tech.imagecorebackenduserservice.interfaces.controller;


import com.tech.imagecorebackendmodel.dto.ai.ChatHisRequest;
import com.tech.imagecorebackendmodel.dto.ai.ChatRequest;
import com.tech.imagecorebackendmodel.vo.ai.ChatAllHisResponse;
import com.tech.imagecorebackendmodel.vo.ai.ChatHisResponse;
import com.tech.imagecorebackenduserservice.application.service.UserAiChatApplicationService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/userAIChat")
public class UserAiChatController {

    @Resource
    private UserAiChatApplicationService userAiChatApplicationService;

    /**
     * AI 聊天调用
     * @param chatRequest
     * @return
     */
    @PostMapping("/doChatStream")
    Flux<String> doChatStream(@RequestBody ChatRequest chatRequest){
        return userAiChatApplicationService.doChatStreamService(chatRequest);
    }

    /**
     * 获取全部聊天历史记录
     * @param chatHisRequest
     * @return
     */
    @PostMapping("/getALLChatHis")
    ChatAllHisResponse getALLChatHisResponse(@RequestBody ChatHisRequest chatHisRequest){
        return userAiChatApplicationService.getALLChatHisResponse(chatHisRequest);
    }

    @PostMapping("/getChatHis")
    ChatHisResponse getChatHisResponse(@RequestBody ChatRequest chatRequest){
        return userAiChatApplicationService.getChatHisResponse(chatRequest);
    }

}

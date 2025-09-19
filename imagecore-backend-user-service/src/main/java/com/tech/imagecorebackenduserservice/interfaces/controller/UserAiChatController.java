package com.tech.imagecorebackenduserservice.interfaces.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.imagecorebackendcommon.common.BaseResponse;
import com.tech.imagecorebackendcommon.common.ResultUtils;
import com.tech.imagecorebackendmodel.dto.ai.ChatHisRequest;
import com.tech.imagecorebackendmodel.dto.ai.ChatRequest;
import com.tech.imagecorebackendmodel.vo.ai.ChatAllHisResponse;
import com.tech.imagecorebackendmodel.vo.ai.ChatHisListVo;
import com.tech.imagecorebackendmodel.vo.ai.ChatHisResponse;
import com.tech.imagecorebackendmodel.vo.ai.ChatHisVo;
import com.tech.imagecorebackenduserservice.application.service.UserAiChatApplicationService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/userAIChat")
public class UserAiChatController {

    @Resource
    private UserAiChatApplicationService userAiChatApplicationService;

    @PostMapping("/getNewChatId")
    public BaseResponse<String> getNewChatId(String chatType, HttpServletRequest request){
        return ResultUtils.success(userAiChatApplicationService.getNewChatId(chatType, request));
    }

    @GetMapping(value = "/chat")
    BaseResponse<String> doChat(ChatRequest chatRequest){
        return ResultUtils.success(userAiChatApplicationService.doAIChat(chatRequest));
    }

    @PostMapping("/getAllUserChatHis")
    BaseResponse<List<ChatHisListVo>> getAllUserChatHis(@RequestBody ChatHisRequest chatHisRequest){
        return ResultUtils.success(userAiChatApplicationService.getUserAllChatHis(chatHisRequest));
    }

    @PostMapping("/getChatHisVoPage")
    BaseResponse<Page<ChatHisVo>> getChatHisVoPage(@RequestBody ChatHisRequest chatHisRequest){
        return ResultUtils.success(userAiChatApplicationService.getChatHisVo(chatHisRequest));
    }

//    /**
//     * AI 聊天调用
//     * @param
//     * @return
//     */
//    @GetMapping(value = "/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    Flux<ServerSentEvent<String>> doChatStream(ChatRequest chatRequest){
//        return userAiChatApplicationService.doChatStreamService(chatRequest);
//    }

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

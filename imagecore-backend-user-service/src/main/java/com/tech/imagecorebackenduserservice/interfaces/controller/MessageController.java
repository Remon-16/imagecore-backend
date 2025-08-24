package com.tech.imagecorebackenduserservice.interfaces.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.imagecorebackendmodel.dto.user.UserMessageRequest;
import com.tech.imagecorebackendmodel.vo.user.MessageVo;
import com.tech.imagecorebackenduserservice.application.service.MessageApplicationService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageApplicationService messageApplicationService;

    @PostMapping("/allMessageREAD")
    public void allMessageREAD(@RequestBody UserMessageRequest userMessageRequest){
        messageApplicationService.allMessageREAD(userMessageRequest.getUserId());
    }

    @PostMapping("/listMessageVoByPage")
    public Page<MessageVo> listMessageVoByPage(@RequestBody UserMessageRequest userMessageRequest, HttpServletRequest request){
        return messageApplicationService.listMessageVoByPage(userMessageRequest, request);
    }

    @PostMapping("/changeMessageStatus")
    public void changeMessageStatus(@RequestBody UserMessageRequest userMessageRequest){
        messageApplicationService.changeMessageStatus(userMessageRequest.getMessageId(), userMessageRequest.getMessageStatus());
    }
}

package com.tech.imagecorebackenduserservice.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.imagecorebackendmodel.dto.user.UserMessageRequest;
import com.tech.imagecorebackendmodel.user.entity.Message;
import com.tech.imagecorebackendmodel.vo.user.MessageVo;
import com.tech.imagecorebackenduserservice.application.service.MessageApplicationService;
import com.tech.imagecorebackenduserservice.domain.user.service.MessageDomainService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class MessageApplicationServiceImpl implements MessageApplicationService {

    @Resource
    MessageDomainService messageDomainService;
    @Override
    public void changeMessageStatus(Long messageId, String status) {
        messageDomainService.changeMessageStatus(messageId, status);
    }

    @Override
    public void messageSend(Message message) {
        messageDomainService.messageSend(message);
    }

    @Override
    public void allMessageREAD(Long userId, String messageType) {
        messageDomainService.allMessageREAD(userId, messageType);
    }
    @Override
    public Page<MessageVo> listMessageVoByPage(UserMessageRequest userMessageRequest, HttpServletRequest request) {
        return messageDomainService.listMessageVoByPage(userMessageRequest, request);
    }

    @Override
    public Boolean getExistUnReadMessage(UserMessageRequest userMessageRequest) {
        return messageDomainService.getExistUnReadMessage(userMessageRequest);
    }
}

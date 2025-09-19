package com.tech.imagecorebackenduserservice.domain.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.imagecorebackendmodel.dto.user.UserMessageRequest;
import com.tech.imagecorebackendmodel.user.entity.Message;
import com.tech.imagecorebackendmodel.vo.user.MessageVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;


public interface MessageDomainService {

    QueryWrapper<Message> getQueryWrapper(UserMessageRequest userMessageRequest);

    void changeMessageStatus(Long messageId, String status);

    void messageSend(Message message);

    void messageBatchSend(List<Message> messages);

    void allMessageREAD(Long userId, String messageType);

    Page<MessageVo> listMessageVoByPage(UserMessageRequest userMessageRequest, HttpServletRequest request);

    Boolean getExistUnReadMessage(UserMessageRequest userMessageRequest);
}

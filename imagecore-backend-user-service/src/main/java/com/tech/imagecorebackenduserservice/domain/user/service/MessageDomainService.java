package com.tech.imagecorebackenduserservice.domain.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.imagecorebackendmodel.dto.user.UserMessageRequest;
import com.tech.imagecorebackendmodel.user.entity.Message;
import com.tech.imagecorebackendmodel.vo.user.MessageVo;
import jakarta.servlet.http.HttpServletRequest;


public interface MessageDomainService {

    QueryWrapper<Message> getQueryWrapper(UserMessageRequest userMessageRequest);

    void changeMessageStatus(Long messageId, String status);

    void messageSend(Message message);

    void allMessageREAD(Long userId);

    Page<MessageVo> listMessageVoByPage(UserMessageRequest userMessageRequest, HttpServletRequest request);
}

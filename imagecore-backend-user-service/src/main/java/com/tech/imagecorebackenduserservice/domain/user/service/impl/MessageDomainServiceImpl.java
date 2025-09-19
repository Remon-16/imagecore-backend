package com.tech.imagecorebackenduserservice.domain.user.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendcommon.exception.ThrowUtils;
import com.tech.imagecorebackendmodel.dto.user.UserMessageRequest;
import com.tech.imagecorebackendmodel.picture.entity.PictureComment;
import com.tech.imagecorebackendmodel.picture.entity.Thumb;
import com.tech.imagecorebackendmodel.user.constant.MessageConstant;
import com.tech.imagecorebackendmodel.user.entity.Message;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendmodel.user.valueobject.MessageType;
import com.tech.imagecorebackendmodel.vo.picture.PictureCommentVo;
import com.tech.imagecorebackendmodel.vo.user.MessageVo;
import com.tech.imagecorebackendmodel.vo.user.UserVO;
import com.tech.imagecorebackendserviceclient.application.service.PictureFeignClient;
import com.tech.imagecorebackenduserservice.domain.user.repository.MessageRepository;
import com.tech.imagecorebackenduserservice.domain.user.service.MessageDomainService;
import com.tech.imagecorebackenduserservice.domain.user.service.UserDomainService;
import com.tech.imagecorebackenduserservice.infrastructure.mapper.MessageMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageDomainServiceImpl extends ServiceImpl<MessageMapper, Message>
        implements MessageDomainService {

    @Resource
    MessageRepository messageRepository;

    @Resource
    MessageMapper messageMapper;

    @Lazy
    @Resource
    UserDomainService userDomainService;

    @Resource
    PictureFeignClient pictureFeignClient;

    @Override
    public Page<MessageVo> listMessageVoByPage(UserMessageRequest userMessageRequest, HttpServletRequest request) {
        long current = userMessageRequest.getCurrent();
        long size = userMessageRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 30, ErrorCode.PARAMS_ERROR);
        Page<Message> messagePage = page(new Page<>(current, size),
                getQueryWrapper(userMessageRequest));

        return getMessageVoPage(messagePage, userMessageRequest.getMessageType());
    }

    @Override
    public QueryWrapper<Message> getQueryWrapper(UserMessageRequest userMessageRequest) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        if (userMessageRequest == null) {
            return queryWrapper;
        }
        Long userId = userMessageRequest.getUserId();
        String messageType = userMessageRequest.getMessageType();
        String sortField = userMessageRequest.getSortField();
        String sortOrder = userMessageRequest.getSortOrder();
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(messageType), "messageType", messageType);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    public Page<MessageVo> getMessageVoPage(Page<Message> messagePage, String messageType) {
        List<Message> messageList = messagePage.getRecords();
        Page<MessageVo> messageVoPage = new Page<>(messagePage.getCurrent(), messagePage.getSize(), messagePage.getTotal());
        if(CollUtil.isEmpty(messageList)){
            return messageVoPage;
        }
        List<MessageVo> messageVoList = messageList.stream().map(MessageVo::objToVo).toList();
        if(MessageType.THUMB.getValue().equals(messageType)){
            messageVoList.forEach((messageVo) -> {
                Thumb thumb = pictureFeignClient.getThumbById(messageVo.getCommentId());
                User user = userDomainService.getById(thumb.getUserId());
                PictureCommentVo pictureCommentVo = new PictureCommentVo();
                UserVO userVO = userDomainService.getUserVO(user);
                pictureCommentVo.setUser(userVO);
                messageVo.setPictureCommentVo(pictureCommentVo);
            });
        } else if (MessageType.COMMENT.getValue().equals(messageType)) {
            messageVoList.forEach((messageVo) -> {
                PictureComment pictureComment = pictureFeignClient.getPictureCommentById(messageVo.getCommentId());
                PictureCommentVo pictureCommentVo = PictureCommentVo.objToVo(pictureComment);
                User user = userDomainService.getById(pictureCommentVo.getUserId());
                UserVO userVO = userDomainService.getUserVO(user);
                pictureCommentVo.setUser(userVO);
                messageVo.setPictureCommentVo(pictureCommentVo);
            });
        }
        messageVoPage.setRecords(messageVoList);
        return messageVoPage;
    }

    @Override
    public void changeMessageStatus(Long messageId, String status) {
        Message message = new Message();
        message.setId(messageId);
        message.setMessageState(status);
        messageRepository.updateById(message);
    }

    @Override
    public void messageSend(Message message) {
        messageRepository.save(message);
    }

    @Override
    public void messageBatchSend(List<Message> messages) {
        messageRepository.saveBatch(messages);
    }

    @Override
    public void allMessageREAD(Long userId, String messageType) {
        messageMapper.updateReadByUser(userId, MessageConstant.MESSAGE_READ, messageType);
    }

    @Override
    public Boolean getExistUnReadMessage(UserMessageRequest userMessageRequest) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        if (userMessageRequest == null) {
            return false;
        }
        Long userId = userMessageRequest.getUserId();
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("messageState", "0");
        Page<Message> messagePage = page(new Page<>(1, 10),
                queryWrapper);
        return !messagePage.getRecords().isEmpty();
    }
}

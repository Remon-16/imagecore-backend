package com.tech.imagecorebackenduserservice.domain.user.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendcommon.exception.ThrowUtils;
import com.tech.imagecorebackendmodel.dto.ai.ChatHisRequest;
import com.tech.imagecorebackendmodel.dto.ai.ChatRequest;
import com.tech.imagecorebackendmodel.user.entity.AiChatMemory;
import com.tech.imagecorebackendmodel.user.entity.UserAIChatHis;
import com.tech.imagecorebackendmodel.user.entity.UserAiChat;
import com.tech.imagecorebackendmodel.vo.ai.ChatAllHisResponse;
import com.tech.imagecorebackendmodel.vo.ai.ChatHisListVo;
import com.tech.imagecorebackendmodel.vo.ai.ChatHisResponse;
import com.tech.imagecorebackendmodel.vo.ai.ChatHisVo;
import com.tech.imagecorebackenduserservice.domain.user.repository.AiChatMemoryRepository;
import com.tech.imagecorebackenduserservice.domain.user.service.UserAiChatDomainService;
import com.tech.imagecorebackenduserservice.domain.user.service.UserDomainService;
import com.tech.imagecorebackenduserservice.infrastructure.ai.service.ChatDomainService;
import com.tech.imagecorebackenduserservice.infrastructure.ai.service.impl.ChatDomainServiceImpl;
import com.tech.imagecorebackenduserservice.infrastructure.mapper.UserAiChatMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class UserAiChatDomainServiceImpl implements UserAiChatDomainService {
    @Resource
    private ChatDomainService chatDomainService;

    @Resource
    private UserAiChatMapper userAiChatMapper;

    @Resource
    private UserDomainService userDomainService;

    @Resource
    private AiChatMemoryRepository aiChatMemoryRepository;

    @Override
    public String getNewChatId(String chatType, HttpServletRequest request) {
        String uuid = chatDomainService.createChatId();
        UserAiChat userAiChat = new UserAiChat();
        userAiChat.setUserId(userDomainService.getLoginUser(request).getId());
        userAiChat.setConversationId(uuid);
        userAiChat.setChatType(chatType);
        userAiChatMapper.insert(userAiChat);
        return uuid;
    }

    @Override
    public String doAIChatService(ChatRequest chatRequest) {
        return chatDomainService.doChatService(chatRequest);
    }

    @Override
    public Flux<ServerSentEvent<String>> doChatStreamService(ChatRequest chatRequest) {
        return chatDomainService.doChatStreamService(chatRequest);
    }

    private QueryWrapper<AiChatMemory> getAiChatMemoryQueryWrapper(String chatId, Date createTime, String sortField, String sortOrder){
        QueryWrapper<AiChatMemory> queryWrapper = new QueryWrapper<>();
        if(StrUtil.isBlank(chatId)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        queryWrapper.eq("conversation_id", chatId);

        if (createTime != null) {
            queryWrapper.lt("timestamp", createTime);
        }
        if ("createTime".equals(sortField)) {
            sortField = "timestamp";
        }
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    private Page<ChatHisVo> getChatHisVoPage(Page<AiChatMemory> aiChatMemoryPage){
        List<AiChatMemory> aiChatMemoryList = aiChatMemoryPage.getRecords();
        List<ChatHisVo> chatHisVoList = new ArrayList<>();
        aiChatMemoryList.forEach(chatMemory -> {
            ChatHisVo chatHisVo = new ChatHisVo();
            chatHisVo.setMessageId(chatMemory.getId());
            chatHisVo.setChatId(chatMemory.getConversation_id());
            chatHisVo.setChatType(chatMemory.getType());
            chatHisVo.setCreateTime(chatMemory.getTimestamp());
            chatHisVo.setChatContent(chatMemory.getContent());
            chatHisVoList.add(chatHisVo);
        });
        Page<ChatHisVo> chatHisVoPage = new Page<>();
        chatHisVoPage.setRecords(chatHisVoList);
        chatHisVoPage.setTotal(aiChatMemoryPage.getTotal());
        chatHisVoPage.setCurrent(aiChatMemoryPage.getCurrent());
        chatHisVoPage.setSize(aiChatMemoryPage.getSize());
        return chatHisVoPage;
    }
    @Override
    public Page<ChatHisVo> getChatHisVo(ChatHisRequest chatHisRequest){
        ThrowUtils.throwIf(chatHisRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(chatHisRequest.getPageSize() > 50, ErrorCode.PARAMS_ERROR);
        List<AiChatMemory> aiChatMemoryList = aiChatMemoryRepository.list(getAiChatMemoryQueryWrapper(chatHisRequest.getChatId(),
                chatHisRequest.getLastChatTime(), chatHisRequest.getSortField(), chatHisRequest.getSortOrder()));
        Page<AiChatMemory> aiChatMemoryPage = new Page<>();
        aiChatMemoryPage.setRecords(aiChatMemoryList);
        aiChatMemoryPage.setTotal(aiChatMemoryList.size());
        aiChatMemoryPage.setCurrent(1);
        return this.getChatHisVoPage(aiChatMemoryPage);
    }

    @Override
    public List<ChatHisListVo> getUserAllChatHis(ChatHisRequest chatHisRequest){
        ThrowUtils.throwIf(chatHisRequest == null, ErrorCode.PARAMS_ERROR, "chatHisRequest 为空");
        ThrowUtils.throwIf(chatHisRequest.getUserId() == null, ErrorCode.PARAMS_ERROR, "userID 为空");
        QueryWrapper<UserAiChat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", chatHisRequest.getUserId());
        queryWrapper.eq("chatType", chatHisRequest.getChatType());
        queryWrapper.orderBy(StrUtil.isNotEmpty(chatHisRequest.getSortField()),
                chatHisRequest.getSortOrder().equals("ascend"), chatHisRequest.getSortField());
        List<UserAiChat> userAiChatList = userAiChatMapper.selectList(queryWrapper);
        List<ChatHisListVo> chatHisListVos = new ArrayList<>();
        ChatHisRequest chatHisRequest1 = new ChatHisRequest();
        chatHisRequest1.setUserId(chatHisRequest.getUserId());
        chatHisRequest1.setChatType(chatHisRequest.getChatType());
        chatHisRequest1.setLastChatTime(chatHisRequest.getLastChatTime());
        chatHisRequest1.setSortField("id");
        chatHisRequest1.setSortOrder("ascend");
        chatHisRequest1.setPageSize(chatHisRequest.getPageSize());

        userAiChatList.forEach(userAiChat -> {
            ChatHisListVo chatHisListVo = new ChatHisListVo();
            chatHisListVo.setChatId(userAiChat.getConversationId());
            chatHisRequest1.setChatId(userAiChat.getConversationId());
            chatHisListVo.setChatHisList(this.getChatHisVo(chatHisRequest1));
            chatHisListVo.setLastCreateTime(chatHisListVo.getChatHisList().getRecords().getLast().getCreateTime());
            chatHisListVos.add(chatHisListVo);
        });
        return chatHisListVos;
    }


    @Override
    public ChatAllHisResponse getALLChatHisResponse(ChatHisRequest chatHisRequest) {
        ThrowUtils.throwIf(chatHisRequest == null, ErrorCode.PARAMS_ERROR, "chatHisRequest 为空");
        ThrowUtils.throwIf(chatHisRequest.getUserId() == null, ErrorCode.PARAMS_ERROR, "userID 为空");

        List<UserAIChatHis> userAllAIChatHisList = userAiChatMapper.queryALLChatHistoryByUser(chatHisRequest.getUserId(), chatHisRequest.getChatType());
        ChatAllHisResponse chatAllHisResponse = new ChatAllHisResponse();
        List<ChatHisListVo> chatHisList = new ArrayList<>();
        String cruChatId = null;
        ChatHisListVo chatHisListVo = null;
        for(UserAIChatHis userAIChatHis : userAllAIChatHisList) {
            String chatId = userAIChatHis.getChatId();
            if(!chatId.equals(cruChatId)) {
                // 存上一个对话
                if(chatHisListVo != null) {
                    chatHisListVo.setLastCreateTime(userAIChatHis.getTimestamp());
                    chatHisListVo.setChatId(chatId);
                    chatHisList.add(chatHisListVo);
                }
                // 初始化
                chatHisListVo = new ChatHisListVo();
                chatHisListVo.setChatHisList(new Page<>());
                cruChatId = chatId;
            }
            ChatHisVo chatHisVo = new ChatHisVo();
            chatHisVo.setChatId(chatId);
            chatHisVo.setChatType(userAIChatHis.getChatType());
            chatHisVo.setChatContent(userAIChatHis.getChatContent());
            chatHisVo.setCreateTime(userAIChatHis.getTimestamp());
            chatHisListVo.getChatHisList().getRecords().add(chatHisVo);
        }
        if(cruChatId != null) {
            chatHisListVo.setChatId(cruChatId);
            chatHisListVo.setLastCreateTime(userAllAIChatHisList.getLast().getTimestamp());
            chatHisList.add(chatHisListVo);
        }
        chatAllHisResponse.setChatHisList(chatHisList);
        return chatAllHisResponse;
    }

    @Override
    public ChatHisResponse getChatHisResponse(ChatRequest chatRequest) {
        ThrowUtils.throwIf(chatRequest == null, ErrorCode.PARAMS_ERROR, "chatHisRequest 为空");
        ThrowUtils.throwIf(chatRequest.getUserId() == null, ErrorCode.PARAMS_ERROR, "userId 为空");
        ThrowUtils.throwIf(chatRequest.getChatId() == null, ErrorCode.PARAMS_ERROR, "chatId 为空");

        List<UserAIChatHis> userAIChatHisList = userAiChatMapper
                .queryChatHistoryByUserAndTime(
                        chatRequest.getUserId(), chatRequest.getChatId(), chatRequest.getLastTimestamp());
        ChatHisResponse chatHisResponse = new ChatHisResponse();
        LocalDateTime lastCreateTime = userAIChatHisList.getLast().getTimestamp().toLocalDateTime();
        chatHisResponse.setLastCreateTime(lastCreateTime);
        List<ChatHisVo> chatHisList = new ArrayList<>();
        for(UserAIChatHis userAIChatHis : userAIChatHisList) {
            ChatHisVo chatHisVo = new ChatHisVo();
            chatHisVo.setChatType(userAIChatHis.getChatType());
            chatHisVo.setChatContent(userAIChatHis.getChatContent());
            chatHisVo.setCreateTime(userAIChatHis.getTimestamp());
            chatHisList.add(chatHisVo);
        }
        chatHisResponse.setChatHisList(chatHisList);
        return chatHisResponse;
    }

}

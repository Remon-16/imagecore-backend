package com.tech.imagecorebackenduserservice.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tech.imagecorebackendmodel.user.entity.UserAIChatHis;
import com.tech.imagecorebackendmodel.user.entity.UserAiChat;


import java.sql.Timestamp;
import java.util.List;

/**
* @author Remon
* @description 针对表【user_ai_chat(用户和AI聊天的关联表)】的数据库操作Mapper
* @createDate 2025-08-04 16:35:19
* @Entity com.tech.imagocorebackend.domain.user.entity.UserAiChat
*/
public interface UserAiChatMapper extends BaseMapper<UserAiChat> {
    List<UserAIChatHis> queryALLChatHistoryByUser(Long userId, String chatType);

    List<UserAIChatHis> queryChatHistoryByUserAndTime(Long userId, String chatId, Timestamp lastTimestamp);
}





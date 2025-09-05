package com.tech.imagecorebackenduserservice.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tech.imagecorebackendmodel.user.entity.Message;
import org.apache.ibatis.annotations.Param;


/**
* @author Remon
* @description 针对表【message(消息表)】的数据库操作Mapper
* @createDate 2025-08-04 16:34:44
* @Entity com.tech.imagocorebackend.domain.user.entity.Message
*/
public interface MessageMapper extends BaseMapper<Message> {
    void updateReadByUser(@Param("userId") Long userId, @Param("messageStatus") String messageStatus, @Param("messageType") String messageType);
}





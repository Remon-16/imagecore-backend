package com.tech.imagecorebackenduserservice.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tech.imagecorebackendmodel.user.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
* @author Remon
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2024-12-09 20:03:03
* @Entity com.tech.imagocorebackend.domain.user.entity.User
*/
public interface UserMapper extends BaseMapper<User> {
    void batchUpdateScore(@Param("scoreMap") Map<Long, Long> scoreMap);
}





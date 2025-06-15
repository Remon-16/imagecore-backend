package com.tech.imagecorebackendpictureservice.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tech.imagecorebackendmodel.picture.entity.Picture;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
* @author Remon
* @description 针对表【picture(图片)】的数据库操作Mapper
* @createDate
* @Entity
*/
public interface PictureMapper extends BaseMapper<Picture> {
    void batchUpdateThumbCount(@Param("countMap") Map<Long, Long> countMap);
}





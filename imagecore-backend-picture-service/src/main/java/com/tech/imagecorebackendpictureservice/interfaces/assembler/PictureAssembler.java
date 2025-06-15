package com.tech.imagecorebackendpictureservice.interfaces.assembler;

import cn.hutool.json.JSONUtil;

import com.tech.imagecorebackendmodel.dto.picture.PictureEditRequest;
import com.tech.imagecorebackendmodel.dto.picture.PictureUpdateRequest;
import com.tech.imagecorebackendmodel.picture.entity.Picture;
import org.springframework.beans.BeanUtils;

/**
 * 图片对象转换
 */
public class PictureAssembler {

    public static Picture toPictureEntity(PictureEditRequest request) {
        Picture picture = new Picture();
        BeanUtils.copyProperties(request, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(request.getTags()));
        return picture;
    }

    public static Picture toPictureEntity(PictureUpdateRequest request) {
        Picture picture = new Picture();
        BeanUtils.copyProperties(request, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(request.getTags()));
        return picture;
    }
}
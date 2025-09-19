package com.tech.imagecorebackendpictureservice.domain.picture.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tech.imagecorebackendmodel.picture.entity.Thumb;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendmodel.dto.picture.DoThumbRequest;
import com.tech.imagecorebackendmodel.vo.picture.PictureVO;

import java.util.List;


/**
* @author Remon
* @description 针对表【thumb】的数据库操作Service
* @createDate
*/
public interface ThumbDomainService extends IService<Thumb> {

    Boolean doThumb(DoThumbRequest doThumbRequest, User loginUser);

    Boolean undoThumb(DoThumbRequest doThumbRequest, User loginUser);

    Boolean hasThumb(Long pictureId, Long userId);

    List<PictureVO> getPictureThumbState(List<PictureVO> pictureVOList, User loginUser);

}

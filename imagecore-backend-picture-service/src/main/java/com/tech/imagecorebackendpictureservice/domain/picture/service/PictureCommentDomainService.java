package com.tech.imagecorebackendpictureservice.domain.picture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tech.imagecorebackendmodel.dto.picture.PictureCommentQueryRequest;
import com.tech.imagecorebackendmodel.dto.picture.PictureCommentRequest;
import com.tech.imagecorebackendmodel.picture.entity.PictureComment;
import com.tech.imagecorebackendmodel.vo.picture.PictureCommentRootVo;
import com.tech.imagecorebackendmodel.vo.picture.PictureCommentVo;


import java.util.List;

public interface PictureCommentDomainService {

    PictureComment saveOrUpdatePictureComment(PictureCommentRequest pictureCommentRequest);

    QueryWrapper<PictureComment> getQueryWrapper(PictureCommentQueryRequest pictureCommentQueryRequest);

    List<PictureCommentRootVo> getPictureCommentRootVo(PictureCommentQueryRequest pictureCommentQueryRequest);

    List<PictureCommentVo> getPictureCommentVo(PictureCommentQueryRequest pictureCommentQueryRequest);


}

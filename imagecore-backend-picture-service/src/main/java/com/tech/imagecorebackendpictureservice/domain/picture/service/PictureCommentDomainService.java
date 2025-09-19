package com.tech.imagecorebackendpictureservice.domain.picture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.imagecorebackendmodel.dto.picture.PictureCommentQueryRequest;
import com.tech.imagecorebackendmodel.dto.picture.PictureCommentRequest;
import com.tech.imagecorebackendmodel.picture.entity.PictureComment;
import com.tech.imagecorebackendmodel.vo.picture.PictureCommentRootVo;
import com.tech.imagecorebackendmodel.vo.picture.PictureCommentVo;
import com.tech.imagecorebackendpictureservice.infrastructure.mq.consumer.CanalHandleVo;


import java.util.List;

public interface PictureCommentDomainService {

    PictureComment saveOrUpdatePictureComment(PictureCommentRequest pictureCommentRequest);

    Boolean deletePictureComment(PictureCommentRequest pictureCommentRequest);

    QueryWrapper<PictureComment> getQueryWrapper(PictureCommentQueryRequest pictureCommentQueryRequest);

    Page<PictureCommentRootVo> getPictureCommentRootVo(PictureCommentQueryRequest pictureCommentQueryRequest);

    Page<PictureCommentVo> getPictureCommentVo(PictureCommentQueryRequest pictureCommentQueryRequest);

    void canalHandlePictureComment(List<CanalHandleVo> canalHandleVoList);

    PictureComment getById(Long pictureCommentId);
}

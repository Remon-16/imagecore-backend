package com.tech.imagecorebackendpictureservice.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.imagecorebackendmodel.dto.picture.PictureCommentQueryRequest;
import com.tech.imagecorebackendmodel.dto.picture.PictureCommentRequest;
import com.tech.imagecorebackendmodel.picture.entity.PictureComment;
import com.tech.imagecorebackendmodel.vo.picture.PictureCommentRootVo;
import com.tech.imagecorebackendmodel.vo.picture.PictureCommentVo;

import java.util.List;

public interface PictureCommentApplicationService {
    PictureComment saveOrUpdatePictureComment(PictureCommentRequest pictureCommentRequest);

    Boolean deletePictureComment(PictureCommentRequest pictureCommentRequest);

    Page<PictureCommentRootVo> getPictureCommentRootVo(PictureCommentQueryRequest pictureCommentQueryRequest);

    Page<PictureCommentVo> getPictureCommentVo(PictureCommentQueryRequest pictureCommentQueryRequest);

    PictureComment getById(Long pictureCommentId);
}

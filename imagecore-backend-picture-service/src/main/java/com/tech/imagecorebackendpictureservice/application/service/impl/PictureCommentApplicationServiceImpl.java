package com.tech.imagecorebackendpictureservice.application.service.impl;

import com.tech.imagecorebackendmodel.dto.picture.PictureCommentQueryRequest;
import com.tech.imagecorebackendmodel.dto.picture.PictureCommentRequest;
import com.tech.imagecorebackendmodel.picture.entity.PictureComment;
import com.tech.imagecorebackendmodel.vo.picture.PictureCommentRootVo;
import com.tech.imagecorebackendmodel.vo.picture.PictureCommentVo;
import com.tech.imagecorebackendpictureservice.application.service.PictureCommentApplicationService;
import com.tech.imagecorebackendpictureservice.domain.picture.service.PictureCommentDomainService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PictureCommentApplicationServiceImpl implements PictureCommentApplicationService {
    @Resource
    PictureCommentDomainService pictureCommentDomainService;

    @Override
    public PictureComment saveOrUpdatePictureComment(PictureCommentRequest pictureCommentRequest) {
        return pictureCommentDomainService.saveOrUpdatePictureComment(pictureCommentRequest);
    }

    @Override
    public Boolean deletePictureComment(PictureCommentRequest pictureCommentRequest) {
        return pictureCommentDomainService.deletePictureComment(pictureCommentRequest);
    }

    @Override
    public List<PictureCommentRootVo> getPictureCommentRootVo(PictureCommentQueryRequest pictureCommentQueryRequest) {
        return pictureCommentDomainService.getPictureCommentRootVo(pictureCommentQueryRequest);
    }

    @Override
    public List<PictureCommentVo> getPictureCommentVo(PictureCommentQueryRequest pictureCommentQueryRequest) {
        return pictureCommentDomainService.getPictureCommentVo(pictureCommentQueryRequest);
    }
}

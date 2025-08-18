package com.tech.imagecorebackendpictureservice.application.service.impl;


import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendmodel.dto.picture.DoThumbRequest;
import com.tech.imagecorebackendpictureservice.application.service.ThumbApplicationService;
import com.tech.imagecorebackendpictureservice.domain.picture.service.ThumbDomainService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class ThumbApplicationServiceImpl implements ThumbApplicationService {
    @Resource
    private ThumbDomainService thumbDomainService;

    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, User loginUser) {
        return thumbDomainService.doThumb(doThumbRequest, loginUser);
    }

    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, User loginUser) {
        return thumbDomainService.undoThumb(doThumbRequest, loginUser);
    }

    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        return thumbDomainService.hasThumb(blogId, userId);
    }
}

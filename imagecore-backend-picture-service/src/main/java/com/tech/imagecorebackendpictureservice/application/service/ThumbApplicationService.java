package com.tech.imagecorebackendpictureservice.application.service;

import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendmodel.vo.picture.DoThumbRequest;

public interface ThumbApplicationService {
    Boolean doThumb(DoThumbRequest doThumbRequest, User loginUser);

    Boolean undoThumb(DoThumbRequest doThumbRequest, User loginUser);

    Boolean hasThumb(Long blogId, Long userId);
}

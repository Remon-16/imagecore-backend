package com.tech.imagecorebackendmodel.dto.space.inner;

import com.tech.imagecorebackendmodel.space.entity.Space;
import com.tech.imagecorebackendmodel.user.entity.User;
import lombok.Data;

@Data
public class SpaceUserAuthRequest {

    private Space space;

    private User loginUser;
    
    private String needPermission;
}

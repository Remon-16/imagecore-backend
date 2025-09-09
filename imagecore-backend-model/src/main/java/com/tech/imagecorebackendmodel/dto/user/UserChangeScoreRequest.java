package com.tech.imagecorebackendmodel.dto.user;

import lombok.Data;

@Data
public class UserChangeScoreRequest {
    String scoreType;
    Long score;
    Long userId;
}


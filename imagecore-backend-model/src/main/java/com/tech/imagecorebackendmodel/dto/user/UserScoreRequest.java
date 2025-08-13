package com.tech.imagecorebackendmodel.dto.user;

import lombok.Data;

@Data
public class UserScoreRequest {
    private Long userId;
    private String scoreType;
    private Long score;
}

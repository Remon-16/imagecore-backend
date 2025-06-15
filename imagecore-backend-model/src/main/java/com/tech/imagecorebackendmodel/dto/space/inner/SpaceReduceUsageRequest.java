package com.tech.imagecorebackendmodel.dto.space.inner;

import lombok.Data;

@Data
public class SpaceReduceUsageRequest {

    private Long spaceId;
    /**
     * 图片体积
     */
    private Long picSize;
}

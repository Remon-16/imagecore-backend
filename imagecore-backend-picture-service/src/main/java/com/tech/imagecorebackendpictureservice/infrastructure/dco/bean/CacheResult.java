package com.tech.imagecorebackendpictureservice.infrastructure.dco.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CacheResult {
    /**
     * 是否为超级热门数据
     */
    private Boolean hotKey;
    /**
     * 缓存值
     */
    private String value;

    /**
     * 点赞数量
     */
    private String countValue;
}

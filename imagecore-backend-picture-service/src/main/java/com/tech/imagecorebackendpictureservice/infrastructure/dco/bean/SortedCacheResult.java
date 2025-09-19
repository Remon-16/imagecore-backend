package com.tech.imagecorebackendpictureservice.infrastructure.dco.bean;

import lombok.Data;

import java.util.Map;

@Data
public class SortedCacheResult {
    private Long total;
    Map<Object, Object> valueMap;
}

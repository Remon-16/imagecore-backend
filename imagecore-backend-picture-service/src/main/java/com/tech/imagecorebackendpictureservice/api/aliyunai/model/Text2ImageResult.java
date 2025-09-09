package com.tech.imagecorebackendpictureservice.api.aliyunai.model;

import lombok.Data;

@Data
public class Text2ImageResult {

    private String orig_prompt;

    private String actual_prompt;

    private String url;

}
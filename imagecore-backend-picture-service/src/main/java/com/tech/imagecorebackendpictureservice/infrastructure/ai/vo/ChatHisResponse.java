package com.tech.imagecorebackendpictureservice.infrastructure.ai.vo;

import lombok.Data;

import java.util.List;

@Data
public class ChatHisResponse {
    private List<List<ChatHisVo>> chatHisList;

}

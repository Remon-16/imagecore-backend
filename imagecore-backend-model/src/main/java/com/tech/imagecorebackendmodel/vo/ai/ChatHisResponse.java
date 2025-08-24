package com.tech.imagecorebackendmodel.vo.ai;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatHisResponse {
    private List<ChatHisVo> chatHisList;

    private LocalDateTime lastCreateTime;
}

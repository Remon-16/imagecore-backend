package com.tech.imagecorebackendpictureservice.infrastructure.mq.consumer;

import com.alibaba.otter.canal.protocol.CanalEntry;
import lombok.Data;

@Data
public class CanalHandleVo {
    private String tableName;
    private CanalEntry.EventType eventType;
    private String jsonDataStr;
}

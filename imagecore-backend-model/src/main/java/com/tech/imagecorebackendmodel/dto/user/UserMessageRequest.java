package com.tech.imagecorebackendmodel.dto.user;


import com.tech.imagecorebackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserMessageRequest extends PageRequest {
    Long userId;
    String messageType;
}

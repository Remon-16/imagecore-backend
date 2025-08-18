package com.tech.imagecorebackendmodel.dto.picture;


import com.tech.imagecorebackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PictureCommentQueryRequest extends PageRequest {
    Long pictureId;
    Long targetId;
}

package com.tech.imagecorebackendspaceservice.interfaces.assembler;

import com.tech.imagecorebackendmodel.dto.space.SpaceAddRequest;
import com.tech.imagecorebackendmodel.dto.space.SpaceEditRequest;
import com.tech.imagecorebackendmodel.dto.space.SpaceUpdateRequest;
import com.tech.imagecorebackendmodel.space.entity.Space;
import org.springframework.beans.BeanUtils;

/**
 * 空间对象转换
 */
public class SpaceAssembler {

    public static Space toSpaceEntity(SpaceAddRequest request) {
        Space space = new Space();
        BeanUtils.copyProperties(request, space);
        return space;
    }

    public static Space toSpaceEntity(SpaceUpdateRequest request) {
        Space space = new Space();
        BeanUtils.copyProperties(request, space);
        return space;
    }

    public static Space toSpaceEntity(SpaceEditRequest request) {
        Space space = new Space();
        BeanUtils.copyProperties(request, space);
        return space;
    }
}
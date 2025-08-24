package com.tech.imagecorebackendmodel.ai.valueobject;


import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;

public enum ChatTypeEnum {
    CS,
    DRAW;

    public static boolean isEnumValue(String input) {
        try {
            ChatTypeEnum.valueOf(input);
            return true;
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "聊天类型不正确");
        }
    }
}

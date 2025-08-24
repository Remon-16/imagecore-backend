package com.tech.imagecorebackendmodel.user.valueobject;

import cn.hutool.core.util.ObjUtil;
import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendmodel.ai.valueobject.ChatTypeEnum;

public enum UserVipEnum {
    MONTH("MONTH", "30"),
    QUARTER("QUARTER", "95"),
    HALF_YEAR("HALF_YEAR", "190"),
    YEAR("YEAR", "370");
    private final String text;

    private final String value;

    UserVipEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static boolean assertEnumValue(String input) {
        try {
            ChatTypeEnum.valueOf(input);
            return true;
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会员类型不正确");
        }
    }

    /**
     * 根据 text 获取枚举
     *
     * @param text 枚举值的 text
     * @return 枚举值
     */
    public static UserRoleEnum getEnumByText(String text) {
        if (ObjUtil.isEmpty(text)) {
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.getText().equals(text)) {
                return userRoleEnum;
            }
        }
        return null;
    }
}

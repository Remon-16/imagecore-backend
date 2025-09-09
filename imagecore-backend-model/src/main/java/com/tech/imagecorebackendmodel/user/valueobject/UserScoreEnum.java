package com.tech.imagecorebackendmodel.user.valueobject;

import cn.hutool.core.util.ObjUtil;
import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendmodel.user.constant.UserScoreConstant;
import lombok.Getter;

@Getter
public enum UserScoreEnum {

    UPLOAD_PICTURE(UserScoreConstant.UPLOAD_PICTURE, 20L, -1L),
    THUMBNAIL_PICTURE(UserScoreConstant.THUMBNAIL_PICTURE, 5L, 5L),
    BETHUMBNAIL_PICTURE(UserScoreConstant.BETHUMBNAIL_PICTURE, 10L, 10L),
    TEXT_TO_IMAGE(UserScoreConstant.TEXT_TO_IMAGE, 10L, 10L),
    MONTH_VIP(UserScoreConstant.MONTH_VIP, 30L, -1L),
    PICTURE_COMMENT(UserScoreConstant.PICTURE_COMMENT, 10L, 10L),;

    private final String scoreType;

    private final Long score;

    private final Long MaxCount;

    UserScoreEnum(String scoreType, Long score, Long MaxCount) {
        this.scoreType = scoreType;
        this.score = score;
        this.MaxCount = MaxCount;
    }

    /**
     * 根据 scoreType 获取枚举
     *
     * @param scoreType 枚举值的 scoreType
     * @return 枚举值
     */
    public static UserScoreEnum getEnumByScoreType(String scoreType) {
        if (ObjUtil.isEmpty(scoreType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        for (UserScoreEnum userScoreEnum : UserScoreEnum.values()) {
            if (userScoreEnum.scoreType.equals(scoreType)) {
                return userScoreEnum;
            }
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
}

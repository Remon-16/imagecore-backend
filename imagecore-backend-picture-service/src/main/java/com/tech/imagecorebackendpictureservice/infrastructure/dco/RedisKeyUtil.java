package com.tech.imagecorebackendpictureservice.infrastructure.dco;

import com.tech.imagecorebackendcommon.utils.CacheUtils;
import com.tech.imagecorebackendmodel.picture.valueobject.ThumbConstant;

public class RedisKeyUtil {

    public static String getUserThumbKey(Long userId) {
        return ThumbConstant.USER_THUMB_KEY_PREFIX + userId.toString();
    }

    /**
     * 获取 临时点赞记录 key
     */
    public static String getTempThumbKey(String time) {
        return ThumbConstant.TEMP_THUMB_KEY_PREFIX + time;
    }

    /**
     * 图片点赞数量记录 pictureId:count
     */
    public static String getPictureThumbKey(Long pictureId){
        return ThumbConstant.THUMB_KEY_PICTURE_PREFIX + pictureId.toString();
    }

    /**
     * 拼接 redis 的 Key 用于分布式 Redis 区分不同服务
     */
    public static String buildRedisKey(String key){
        return CacheUtils.APP_NAME + ":" + key;
    }

}
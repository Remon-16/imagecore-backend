package com.tech.imagecorebackendcommon.utils;

import cn.hutool.json.JSONUtil;
import org.springframework.util.DigestUtils;

public class CacheUtils {
    /**
     * 项目名称
     */
    public static final String APP_NAME = "imagocore";
    /**
     * 图片查询缓存名称 pic:pictureId
     */
    public static final String PICTURE_CACHE = "pic";

    /**
     * 图片查询接口缓存名称 pic:query:queryCond(md5)
     */
    public static final String PICTURE_QUERY_CACHE = "pic:query";
    /**
     * 用户积分接口缓存名称 user:score:userId
     */
    public static final String USER_SCORE_CACHE = "user:score";
    /**
     * 用户积分接口临时缓存名称 user:score:userId
     */
    public static final String USER_TEMP_SCORE_CACHE = "user:score:temp";
    /**
     * 用户添加积分的上限
     */
    public static final String USER_SCORE_COUNT_CACHE = "user:scoreCount";

    public static String getUserScoreCountKey(String scoreType, String userIdKey){
        return USER_SCORE_COUNT_CACHE + ":" + scoreType + ":" + userIdKey;
    }

    public static String getUserScoreCacheKey(String key){
        return USER_SCORE_CACHE + ":" + key;
    }

    public static String getUserTempScoreCacheKey(String key, String timeSlice){
        return USER_TEMP_SCORE_CACHE + ":" + key + ":" + timeSlice;
    }

    public static String getPictureCacheKey(String key){
        return PICTURE_CACHE + ":" + key;
    }

    public static String getPictureQueryCacheKey(Object queryCondition){
        String queryConditionString = JSONUtil.toJsonStr(queryCondition);
        String hashKey = DigestUtils.md5DigestAsHex(queryConditionString.getBytes());
        return PICTURE_QUERY_CACHE + ":" + hashKey;
    }

    public static String getSinglePictureQueryCacheKey(long id){
        return PICTURE_QUERY_CACHE + ":" + id;
    }

}

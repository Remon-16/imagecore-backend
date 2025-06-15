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

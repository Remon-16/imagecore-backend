package com.tech.imagecorebackendcommon.utils;

import cn.hutool.json.JSONUtil;
import org.springframework.util.DigestUtils;

/**
 * 这个类是缓存键的类，为本地缓存做了预留，所以前面没有拼APP_NAME
 */
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
     * 图片get/vo接口缓存名称
     */
    public static final String PICTURE_QUERY_DETAIL_CACHE = "pic:single";
    /**
     * 图片评论 zSet缓存名称：pic:comment:sorted:(desc/asc):pictureId
     */
    public static final String PICTURE_COMMENT_SORTED_CACHE = "pic:comment:sorted";
    /**
     * 图片评论 评论总数缓存名称 pic:comment:sorted:total:pictureId
     */
    public static final String PICTURE_COMMENT_SORTED_TOTAL_CACHE = "pic:comment:sorted:total";
    /**
     * 图片二级评论 zSet缓存名称 comment:second:sorted:(desc/asc):commentId
     */
    public static final String PICTURE_SECOND_COMMENT_SORTED_CACHE = "comment:second:sorted";
    /**
     * 图片二级评论 总数缓存名称 comment:second:sorted:total:commentId
     */
    public static final String PICTURE_SECOND_COMMENT_SORTED_TOTAL_CACHE = "comment:second:sorted:total";
    /**
     * 图片评论接口缓存 pic:comment:commentId
     */
    public static final String PICTURE_COMMENT_CACHE = "pic:comment";

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

    public static final String DESC = "descend";

    public static final String ASC = "ascend";

    public static String getUserScoreCountKey(String scoreType, String userIdKey){
        return USER_SCORE_COUNT_CACHE + ":" + scoreType + ":" + userIdKey;
    }

    public static String getUserScoreCacheKey(String key){
        return USER_SCORE_CACHE + ":" + key;
    }

    public static String getUserTempScoreCacheKey(String timeSlice){
        return USER_TEMP_SCORE_CACHE + ":" + timeSlice;
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
        return PICTURE_QUERY_DETAIL_CACHE + ":" + id;
    }

    public static String getPictureSortedCommentCacheKey(String order, Long pictureId){
        return PICTURE_COMMENT_SORTED_CACHE + ":" + order + ":" + pictureId;
    }

    public static String getPictureSecondCommentSortedCache(String order, Long commentId){
        return PICTURE_SECOND_COMMENT_SORTED_CACHE + ":" + order + ":" + commentId;
    }

    public static String getPictureCommentCacheKey(String commentId){
        return PICTURE_COMMENT_CACHE + ":" + commentId;
    }

    public static String getPictureCommentSortedTotalCache(Long pictureId){
        return PICTURE_COMMENT_SORTED_TOTAL_CACHE + ":" + pictureId;
    }

    public static String getPictureSecondCommentSortedTotalCache(Long commentId){
        return PICTURE_SECOND_COMMENT_SORTED_TOTAL_CACHE + ":" + commentId;
    }

    public static String getHexLockString(Object queryCondition){
        String queryConditionString = JSONUtil.toJsonStr(queryCondition);
        return DigestUtils.md5DigestAsHex(queryConditionString.getBytes());
    }

}

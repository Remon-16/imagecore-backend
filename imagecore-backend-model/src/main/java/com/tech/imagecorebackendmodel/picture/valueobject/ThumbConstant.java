package com.tech.imagecorebackendmodel.picture.valueobject;

public interface ThumbConstant {

    /**
     * 用户点赞 hash key
     */
    String USER_THUMB_KEY_PREFIX = "thumb:";

    Long UN_THUMB_CONSTANT = 0L;

    /**
     * 临时 点赞记录 key
     */
    String TEMP_THUMB_KEY_PREFIX = "thumb:temp:";

    /**
     * 图片点赞数量记录 pictureId:count
     */
    String THUMB_KEY_PICTURE_PREFIX = "thumb:picture:";

}
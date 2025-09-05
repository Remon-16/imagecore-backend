package com.tech.imagecorebackendmodel.user.valueobject;

public enum MessageType {

    SYSTEM("系统消息", "system"),
    THUMB("点赞消息", "thumb"),
    COMMENT("评论消息", "reply");

    private final String text;

    private final String value;

    MessageType(String text, String value) {
        this.text = text;
        this.value = value;
    }

}

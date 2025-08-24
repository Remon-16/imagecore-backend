package com.tech.imagecorebackendmodel.user.entity;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class UserAIChatHis implements Serializable {

    private String chatContent;

    private String chatType;

    private String chatId;

    private Timestamp timestamp;
}

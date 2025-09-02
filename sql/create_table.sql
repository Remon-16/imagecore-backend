-- 创建库
create database if not exists imagocore_base;

-- 切换库
use imagocore_base;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 图片表
create table if not exists picture
(
    id           bigint auto_increment comment 'id' primary key,
    url          varchar(512)                       not null comment '图片 url',
    name         varchar(128)                       not null comment '图片名称',
    introduction varchar(512)                       null comment '简介',
    category     varchar(64)                        null comment '分类',
    tags         varchar(512)                       null comment '标签（JSON 数组）',
    picSize      bigint                             null comment '图片体积',
    picWidth     int                                null comment '图片宽度',
    picHeight    int                                null comment '图片高度',
    picScale     double                             null comment '图片宽高比例',
    picFormat    varchar(32)                        null comment '图片格式',
    userId       bigint                             not null comment '创建用户 id',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_name (name),                 -- 提升基于图片名称的查询性能
    INDEX idx_introduction (introduction), -- 用于模糊搜索图片简介
    INDEX idx_category (category),         -- 提升基于分类的查询性能
    INDEX idx_tags (tags),                 -- 提升基于标签的查询性能
    INDEX idx_userId (userId)              -- 提升基于用户 ID 的查询性能
) comment '图片' collate = utf8mb4_unicode_ci;


ALTER TABLE picture
    -- 添加新列
    ADD COLUMN reviewStatus INT DEFAULT 0 NOT NULL COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
    ADD COLUMN reviewMessage VARCHAR(512) NULL COMMENT '审核信息',
    ADD COLUMN reviewerId BIGINT NULL COMMENT '审核人 ID',
    ADD COLUMN reviewTime DATETIME NULL COMMENT '审核时间';

-- 创建基于 reviewStatus 列的索引
CREATE INDEX idx_reviewStatus ON picture (reviewStatus);

ALTER TABLE picture
    -- 添加新列
    ADD COLUMN thumbnailUrl varchar(512) NULL COMMENT '缩略图 url';


-- 空间表
create table if not exists space
(
    id         bigint auto_increment comment 'id' primary key,
    spaceName  varchar(128)                       null comment '空间名称',
    spaceLevel int      default 0                 null comment '空间级别：0-普通版 1-专业版 2-旗舰版',
    maxSize    bigint   default 0                 null comment '空间图片的最大总大小',
    maxCount   bigint   default 0                 null comment '空间图片的最大数量',
    totalSize  bigint   default 0                 null comment '当前空间下图片的总大小',
    totalCount bigint   default 0                 null comment '当前空间下的图片数量',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    -- 索引设计
    index idx_userId (userId),        -- 提升基于用户的查询效率
    index idx_spaceName (spaceName),  -- 提升基于空间名称的查询效率
    index idx_spaceLevel (spaceLevel) -- 提升按空间级别查询的效率
) comment '空间' collate = utf8mb4_unicode_ci;

-- 添加新列
ALTER TABLE picture
    ADD COLUMN spaceId bigint  null comment '空间 id（为空表示公共空间）';

-- 创建索引
CREATE INDEX idx_spaceId ON picture (spaceId);

-- 添加新列
ALTER TABLE picture
    ADD COLUMN picColor varchar(16) null comment '图片主色调';

-- 支持空间类型，添加新列
ALTER TABLE space
    ADD COLUMN spaceType int default 0 not null comment '空间类型：0-私有 1-团队';

CREATE INDEX idx_spaceType ON space (spaceType);

-- 空间成员表
create table if not exists space_user
(
    id         bigint auto_increment comment 'id' primary key,
    spaceId    bigint                                 not null comment '空间 id',
    userId     bigint                                 not null comment '用户 id',
    spaceRole  varchar(128) default 'viewer'          null comment '空间角色：viewer/editor/admin',
    createTime datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    -- 索引设计
    UNIQUE KEY uk_spaceId_userId (spaceId, userId), -- 唯一索引，用户在一个空间中只能有一个角色
    INDEX idx_spaceId (spaceId),                    -- 提升按空间查询的性能
    INDEX idx_userId (userId)                       -- 提升按用户查询的性能
) comment '空间用户关联' collate = utf8mb4_unicode_ci;


-- 添加新列
ALTER TABLE picture
    ADD COLUMN thumbCount  bigint  null DEFAULT 0 comment '点赞数量';

-- 点赞记录表
create table if not exists thumb
(
    id         bigint auto_increment
        primary key,
    userId     bigint                             not null,
    pictureId     bigint                             not null,
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间'
)comment '点赞记录' collate = utf8mb4_unicode_ci;
create unique index idx_userId_pictureId
    on thumb (userId, pictureId);

-- 用户积分表
create table if not exists score_user
(
    id         bigint auto_increment primary key,
    userId     bigint                                 not null comment '用户 id',
    scoreAmount      bigint                                 not null comment '积分变动值',
    scoreType  varchar(128)                           not null comment '积分变动类型',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',

    INDEX idx_userId (userId)       -- 提升基于用户的查询效率
)comment '用户积分' collate = utf8mb4_unicode_ci;

-- 添加新列
ALTER TABLE user
    ADD COLUMN userScore  bigint  null DEFAULT 0 comment '积分余额';

-- 添加新列
ALTER TABLE user
    ADD COLUMN vipExpiry  datetime  null  comment '会员到期时间';

-- 添加新列
ALTER TABLE user
    ADD COLUMN vipType  varchar(128)  null DEFAULT 0  comment '会员类型';

-- 用户和AI聊天的关联表
create table if not exists user_ai_chat(
    id         bigint auto_increment primary key,
    userId     bigint                                 not null comment '用户 id',
    conversationId varchar(256)                      not null comment 'chat id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',

    INDEX idx_userId (userId)       -- 提升基于用户的查询效率
)comment '用户和AI聊天的关联表' collate = utf8mb4_unicode_ci;

-- 消息表
create table if not exists message(
    id         bigint auto_increment primary key,
    userId     bigint                                 not null comment '用户 id',
    content    longtext                               not null comment '消息内容',
    messageType varchar(128)                          not null comment '消息类型',
    messageState varchar(128)                         not null comment '消息状态', -- 0 未读 1 已读
    senderId   bigint                                 not null comment '发送者 id', -- 系统消息用户： system001
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',

    INDEX idx_userId (userId)       -- 提升基于用户的查询效率
)comment '消息表' collate = utf8mb4_unicode_ci;

-- 图片评论表
create table if not exists picture_comment(
    id         bigint auto_increment primary key,
    userId     bigint                                 not null comment '用户 id',
    pictureId  bigint                                 not null comment '图片 id',
    targetId   bigint                                 null comment '目标 id 为空代表是直接评论在图片上，不为空说明是多级评论',
    content    varchar(1024)                          not null comment '评论内容',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',

    INDEX idx_userId (userId),       -- 提升基于用户的查询效率
    INDEX idx_pictureId (pictureId)       -- 提升基于用户的查询效率
)comment '评论表' collate = utf8mb4_unicode_ci;

-- 优化直接评论查询
ALTER TABLE picture_comment ADD INDEX idx_picture_target (pictureId, targetId);

-- 优化子评论查询
ALTER TABLE picture_comment ADD INDEX idx_target_time (targetId, createTime);

-- 添加新列
ALTER TABLE picture_comment
    ADD COLUMN secondTargetId  bigint  null  comment '二级目标评论Id';
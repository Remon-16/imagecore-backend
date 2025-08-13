package com.tech.imagecorebackendcommon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AddScore {
    // 增加的积分数
    long value() default 0L;
    // 积分类型（预留）
    String type() default "default";
    // 每天增加的上限次数
    long maxCount() default 0L;
}

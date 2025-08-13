package com.tech.imagecorebackendcommon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DeductScore {
    // 扣除的积分数
    long value() default 0L;
    String type() default "";
    // 是否允许负积分
    boolean allowNegative() default false;
    long maxCount() default 0L;
}

package com.yejunyu.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * @author: YeJunyu
 * @description:
 * @email: yyyejunyu@gmail.com
 * @date: 2020/9/6
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YService {
    String value() default "";
}

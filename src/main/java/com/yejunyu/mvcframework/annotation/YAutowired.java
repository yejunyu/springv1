package com.yejunyu.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author: YeJunyu
 * @description:
 * @email: yyyejunyu@gmail.com
 * @date: 2020/9/6
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YAutowired {
    String value() default "";
}

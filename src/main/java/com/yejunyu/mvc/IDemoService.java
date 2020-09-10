package com.yejunyu.mvc;

import com.yejunyu.spring.framework.annotation.YService;

/**
 * @author: YeJunyu
 * @description:
 * @email: yyyejunyu@gmail.com
 * @date: 2020/9/7
 */
@YService
public interface IDemoService {

    String hello(String name, String from);
}

package com.yejunyu.mvc;

import com.yejunyu.mvcframework.annotation.YAutowired;
import com.yejunyu.mvcframework.annotation.YController;
import com.yejunyu.mvcframework.annotation.YRequestMapping;
import com.yejunyu.mvcframework.annotation.YRequestParam;

/**
 * @author: YeJunyu
 * @description:
 * @email: yyyejunyu@gmail.com
 * @date: 2020/9/7
 */
@YController
@YRequestMapping("/y")
public class DemoController {

    @YAutowired
    IDemoService demoService;


    @YRequestMapping("/hello")
    public String hello(@YRequestParam String name, @YRequestParam String from) {
        return demoService.hello(name, from);
    }
}

package com.yejunyu.mvc;

import com.yejunyu.mvcframework.annotation.YAutowire;
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

    @YAutowire
    IDemoService demoService;


    @YRequestMapping("/hello")
    public String hello(@YRequestParam("name") String name, @YRequestParam("from") String from) {
        return demoService.hello(name, from);
    }
}

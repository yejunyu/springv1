package com.yejunyu;

import java.lang.reflect.Field;

/**
 * @author: YeJunyu
 * @description:
 * @email: yyyejunyu@gmail.com
 * @date: 2020/9/9
 */
public class Test {

    static class A{
        String a;
        String b;
    }

    public static void main(String[] args) {
        A a = new A();
        String simpleName = a.getClass().getSimpleName();
        Field[] fields = a.getClass().getDeclaredFields();
        for (Field field : fields) {

        }
        System.out.println(a.a);
        System.out.println(a.b);
    }
}

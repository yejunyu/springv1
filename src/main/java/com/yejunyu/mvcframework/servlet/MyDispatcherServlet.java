package com.yejunyu.mvcframework.servlet;

import com.yejunyu.mvcframework.annotation.YController;
import com.yejunyu.mvcframework.annotation.YService;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * @author: YeJunyu
 * @description:
 * @email: yyyejunyu@gmail.com
 * @date: 2020/9/3
 */
public class MyDispatcherServlet extends HttpServlet {

    private Map<String, Object> ioc = new HashMap<>();

    private Properties contextConfig = new Properties();

    private List<String> classNameList = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        doDispatch(req, resp);
    }

    /**
     * 执行网络请求委派
     *
     * @param req
     * @param resp
     */
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) {
        // 6. 根据 url调用具体的方法
    }

    @Override
    public void init(ServletConfig config) {
        // 1. 读取配置文件 (扫描配置)
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        // 2. 扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));

        // 3. 实例化相关的类, 并且缓存到 ioc 容器中
        doInstance();

        // 4. 完成依赖注入
        doAutowirted();

        // 5. 初始化 HandlerMapping
        doInitHandlerMapping();

        System.out.println("Spring init finish~");
    }

    private void doInitHandlerMapping() {

    }

    private void doAutowirted() {

    }

    private void doInstance() {
        if (classNameList.isEmpty()) {
            return;
        }
        for (String className : classNameList) {
            try {
                // 有注解的,需要用到的类实例化
                Class<?> aClass = Class.forName(className);
                String beanName = aClass.getSimpleName();
                if (aClass.isAnnotationPresent(YController.class)) {
                    // 首字母小写
                    Object instance = aClass.newInstance();
                    ioc.put(beanName, instance);
                } else if (aClass.isAnnotationPresent(YService.class)) {
                    YService service = aClass.getAnnotation(YService.class);
                    // 同名类如果有自定义的名字就用自定义的
                    if (!"".equals(service.value())) {
                        beanName = service.value();
                    }
                    // 如果 service 是接口,则取出所有实现类
                    // @autowire IService service 但是如果这个接口有多个实现类那就找不到是哪个了
                    for (Class<?> i : aClass.getInterfaces()) {
                        beanName = i.getSimpleName();
                        if (ioc.containsKey(beanName)) {
                            throw new Exception("the beanName is exists!");
                        }
                    }
                    ioc.put(beanName, aClass.newInstance());
                } else {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void doScanner(String scanPackage) {
        // 把包路径替换成文件路径
        URL url = this.getClass().getClassLoader()
                .getResource(scanPackage.replaceAll("\\.", "/"));
        // 变成文件才能解析
        File classPath = new File(Objects.requireNonNull(url).getFile());
        for (File file : Objects.requireNonNull(classPath.listFiles())) {
            // 拼接全类名
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                // 只存 class 文件
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = scanPackage + "." + file.getName().replace(".class", "");
                classNameList.add(className);
            }
        }

    }

    private void doLoadConfig(String contextConfigLocation) {
        // 从 classpath 下读取配置文件,通过 key contextConfigLocation找到 application.properties
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        MyDispatcherServlet servlet = new MyDispatcherServlet();
        servlet.contextConfig = new Properties();
        InputStream is = servlet.getClass().getClassLoader().getResourceAsStream("application.properties");
        servlet.contextConfig.load(is);
        String scanPackage = servlet.contextConfig.getProperty("scanPackage");
        System.out.println(scanPackage);
        // 把包路径替换成文件路径
        URL url = servlet.getClass().getClassLoader()
                .getResource(scanPackage.replaceAll("\\.", "/"));
        System.out.println(url);
        // 变成文件才能解析
        File classPath = new File(url.getFile());
        System.out.println(classPath);
    }
}

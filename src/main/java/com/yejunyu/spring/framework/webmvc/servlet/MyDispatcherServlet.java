package com.yejunyu.spring.framework.webmvc.servlet;

import com.yejunyu.spring.framework.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    private Map<String, Method> handlerMapping = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception :" + Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * 执行网络请求委派
     *
     * @param req
     * @param resp
     */
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {
        // 6. 根据 url调用具体的方法
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        System.out.println("url is :" + url + " contextPath is :" + contextPath);
        url = ("/" + url).replaceAll(contextPath, "").replaceAll("/+", "");
        Map<String, String[]> parameterMap = req.getParameterMap();
        Method method = handlerMapping.get(url);
        if (method == null) {
            resp.getWriter().write("404 NOT FOUND!");
            return;
        }
        // 形参
        Class<?>[] parameterTypes = method.getParameterTypes();
        // 实参
        Object[] objects = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class) {
                objects[i] = req;
            } else if (parameterType == HttpServletResponse.class) {
                objects[i] = resp;
            } else if (parameterType == String.class) {
                Annotation[][] annotations = method.getParameterAnnotations();
                for (Annotation a : annotations[i]) {
                    if (a instanceof YRequestParam) {
                        String paramName = ((YRequestParam) a).value();
                        if (!"".equals(paramName)) {
                            // param 是用数组形式呈现的
                            String value = Arrays.toString(parameterMap.get(paramName));
                            objects[i] = value;
                        }
                    }
                }
            } else {
                // todo 其他类型的参数
                objects[i] = null;
            }
        }
        // 拿出 method 的类名
        String beanName = method.getDeclaringClass().getSimpleName();
        method.invoke(ioc.get(beanName));
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
        doAutowire();

        // 5. 初始化 HandlerMapping
        doInitHandlerMapping();

        System.out.println("Spring init finish~");
    }

    /**
     * 把 url 和 Controller 执行的方法映射起来
     */
    private void doInitHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> aClass = entry.getValue().getClass();
            if (!aClass.isAnnotationPresent(YController.class)) {
                continue;
            }
            String baseUrl = "";
            if (aClass.isAnnotationPresent(YRequestMapping.class)) {
                YRequestMapping requestMapping = aClass.getAnnotation(YRequestMapping.class);
                baseUrl = requestMapping.value();
            }
            Method[] methods = aClass.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(YRequestMapping.class)) {
                    continue;
                }
                YRequestMapping requestMapping = method.getAnnotation(YRequestMapping.class);
                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                handlerMapping.put(url, method);
                System.out.println("Mapped url: " + url + "," + method);
            }
        }
    }

    private void doAutowire() {
        if (ioc.isEmpty()) {
            return;
        }
        // @autowire Service service,相当于 Service service = new Service()
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            // Controller 或者 service 里的属性
            // 把 service 赋值 new Service()
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                // 给有 autowire 注解的属性赋值
                if (!field.isAnnotationPresent(YAutowire.class)) {
                    continue;
                }
                YAutowire autowire = field.getAnnotation(YAutowire.class);
                String beanName = autowire.value().trim();
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }
                // 一般都是私有属性,所以要加暴力访问
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
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
                        System.out.println(beanName);
                        if (ioc.containsKey(beanName)) {
                            throw new Exception("the beanName is exists!");
                        }
                        ioc.put(beanName, i.newInstance());
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
        servlet.doScanner(scanPackage);
        servlet.doInstance();
        servlet.doAutowire();
        servlet.doInitHandlerMapping();
    }
}

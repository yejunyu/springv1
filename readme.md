# spring

### 1. 配置阶段

1. 配置web.xml > DispatcherServlet
2. 设定init-param > contextConfigLocation = classpath:application.xml
3. 设定url-pattern > /*
4. 配置Annotation > @Controller @Service @Autowired ...

### 2. 初始化阶段

1. 调用init()方法 > 加载配置文件
2. IOC容器初始化 > Map<String, Object>
3. 扫描相关的类 > scan-package = "com.yejunyu"
4. 创建实例化并保存至容器 > 通过反射机制将类实例化放入IOC容器中
5. 进行DI操作 > 扫描IOC容器中的实例,给没有赋值的属性自动赋值
6. 初始化HandlerMapping > 讲一个URL和一个Method进行一对一的关联映射Map<String,Method>

### 3. 运行阶段阶段

1. 调用doPost()/doGet() > Web容器调用doPost/doGet方法,活动request/response对象
2. 匹配 HandlerMapping > 从 request 对象中获得用户输入的 url,找到其对应的 Method
3. 反射调用 method.invoker() > 利用反射调用方法返回结果
4. response.getWrite().write() > 将返回结果输出到浏览器

### 使用方式有两种
1. application.properties里定义扫描包
2. 通过配置类来读取配置定义扫描包
例如:
```
AnnotationConfigWebApplicationContext ctx1 = new AnnotationConfigWebApplicationContext();
Object bean = ctx1.getBean("");
```
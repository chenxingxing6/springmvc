## 手写SpringMvc[尽可能模仿SpringMvc源码]

###### 更新日志 2019.9.25
> 1.支持多种请求 Get、Post...    
> @RequestMapping(value = "/login", method = RequestMethod.POST)

###### 更新日志 2019.9.21 
 > 1. 修改前端控制器，模仿spirngmvc源码   
 > 2. 更加模块化   
 > 3. 引入ApplicationContext，配置放入上下文中   
 > 4. 需要看原来以前简单的代码，可以看以前的提交记录  
 ```html
public void initStrategies(ApplicationContext context){
   // 请求解析
   initMultipartResolver(context);
   // 多语言、国际化解析
   initLocaleResolver(context);
   // 主题View解析
   initThemeResolver(context);
   // 解析url和Method的关联关系
   initHandlerMappings(context);
   // 适配器（匹配的过程）
   initHandlerAdapters(context);
   // 异常解析
   initHandlerExceptionResolvers(context);
   // 视图转发（根据视图名字匹配到一个具体模板）
   initRequestToViewNameTranslator(context);
   // 解析模板中的内容（拿到服务器传过来的数据，生成HTML代码）
   initViewResolvers(context);
   initFlashMapManager(context);
}
```
 


---

![avatar](https://raw.githubusercontent.com/chenxingxing6/springmvc/master/img/44.jpg)


---
![avatar](https://raw.githubusercontent.com/chenxingxing6/springmvc/master/img/55.jpg)

项目结构
![avatar](https://raw.githubusercontent.com/chenxingxing6/springmvc/master/img/33.jpg)


项目启动（jetty插件）
![avatar](https://raw.githubusercontent.com/chenxingxing6/springmvc/master/img/88.jpg)


---
##### 登陆测试Demo
![avatar](https://raw.githubusercontent.com/chenxingxing6/springmvc/master/img/11.jpg)
![avatar](https://raw.githubusercontent.com/chenxingxing6/springmvc/master/img/22.jpg)


---
```html
@RequestMapping("/login")
    public MyModeAndView login(@RequestParam("name") String name, @RequestParam("pwd") String pwd){
        MyModeAndView modeAndView = new MyModeAndView();
        modeAndView.setViewName("index");
        modeAndView.addObject("name", name);
        return modeAndView;
    }
```

---
访问地址：http://localhost:8080/test/view?path=login

```html
<html>
<head>
    <title>登陆</title>
</head>
<body>
<div>
    <h1>欢迎登陆</h1>
    <form action="/test/login" method="get">
   <div>
       <label>用户名：</label>
       <input name="name" type="text"/>
   </div>
    <div>
        <label>用户名：</label>
        <input name="pwd" type="password"/>
    </div>
    <div>
        <label></label>
        <input type="submit" value="提交"/>
    </div>
    </form>
</div>
</body>
</html>

```

---
```html
<html>
<head>
    <title>Title</title>
</head>
<body>
<div>
    <h1>手写SpringMvc</h1>
    <h3 style="color: blue;">欢迎 ${name} 你使用本系统....</h3>
</div>
</body>
</html>
```

---
核心代码
```java
package org.springframework.servlet;

import com.alibaba.fastjson.JSON;
import org.springframework.annotation.*;
import org.springframework.core.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @Author: cxx
 * @Date: 2019/8/27 22:45
 */
public class MyDispatcherServlet extends HttpServlet{
    // 存放配置信息
    Properties props = null;

    // 所有的类名
    private List<String> classNames = null;

    // 实例化对象
    Map<String, Object> ioc = null;

    // Handler
    List<Handler> handlers;

    // 默认适配器
    IHandlerAdapter defaultHandlerAdapter = null;

    public MyDispatcherServlet(){
        props = new Properties();
        classNames = new ArrayList<>();
        ioc = new ConcurrentHashMap<>();
        handlers = new ArrayList<>();
        defaultHandlerAdapter = new DefaultHandlerAdapter();
    }

    // 初始化
    @Override
    public void init(ServletConfig config) throws ServletException {
        System.out.println("------ My mvc is init start...... ------");
        // 1.加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        System.out.println("------ 1.加载配置文件成功-doLoadConfig() ------");

        // 2.根据配置文件扫描所有相关类
        doScanner(props.getProperty("scanPackage"));
        System.out.println("------ 2.扫描所有相关类-ddoScanner() ------");

        // 3.初始化所有相关类的实例，并将放入IOC容器中
        doInstance();
        System.out.println("------ 3.实例化成功-doInstance() ------");

        // 4.实现DI
        doAutowried();
        System.out.println("------ 4.依赖注入成功-doAutowried() ------");

        // 5.初始化HandlerMapping
        initHandlerMapping();
        System.out.println("------ 5.HandlerMapping初始化成功-initHandlerMapping() ------");

        System.out.println("------ My mvc is init end...... ------");
    }

    public void doLoadConfig(String location){
        String configName = location.split(":")[1];
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(configName);
        try {
            props.load(is);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (is != null){
                try {
                    is.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void doScanner(String packageName){
        // 进行递归扫描
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replace(".", "/"));
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if (file.isDirectory()){
                doScanner(packageName + "." + file.getName());
            }else {
                String className = packageName + "." + file.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }

    /**
     * IOC容器规则 key-value
     * 1.key默认用类名小写字段，否则优先使用用户自定义名字
     * 2.如果是接口，用接口的类型作为key
     */
    public void doInstance(){
        if (classNames.isEmpty()){
            return;
        }
        // 利用反射，将扫描的className进行初始化
        try {
            for (String className : classNames) {
                Class clazz = Class.forName(className);
                // 进行Bean实例化，初始化IOC
                if (clazz.isAnnotationPresent(Controller.class)){
                    String beanName = lowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName, clazz.newInstance());
                }else if (clazz.isAnnotationPresent(Service.class)){
                    Service service = (Service) clazz.getAnnotation(Service.class);
                    String beanName = service.value();
                    if ("".equals(beanName.trim())){
                        beanName = lowerFirstCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);

                    // 接口也需要注入,接口类型作为key
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> i : interfaces) {
                        ioc.put(i.getName(), instance);
                    }
                }else {
                    continue;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void doAutowried(){
        if (ioc.isEmpty()){
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            // 获取到所有字段,不管什么类型，都强制注入
            Field[] field = entry.getValue().getClass().getDeclaredFields();
            for (Field f : field) {
                if (f.isAnnotationPresent(Autowired.class)){
                    Autowired autowired = f.getAnnotation(Autowired.class);
                    String beanName = autowired.value().trim();
                    if ("".equals(beanName)){
                        // com.demo.service.ITestService
                        beanName = f.getType().getName();
                    }
                    // 不管愿不愿意，都需要强吻
                    f.setAccessible(true);
                    try {
                        // 例如：TestController -> TestService
                        f.set(entry.getValue(), ioc.get(beanName));
                    }catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }
        }
    }


    public void initHandlerMapping(){
        if (ioc.isEmpty()){
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(Controller.class)){
                continue;
            }
            String baseUrl = "";
            if (clazz.isAnnotationPresent(RequestMapping.class)){
                RequestMapping requestMapping = (RequestMapping)clazz.getAnnotation(RequestMapping.class);
                baseUrl = requestMapping.value();
            }

            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(RequestMapping.class)){
                    continue;
                }
                RequestMapping requestMapping = (RequestMapping)method.getAnnotation(RequestMapping.class);
                String regex = ("/" + baseUrl + requestMapping.value()).replaceAll("/+", "/");
                Pattern pattern = Pattern.compile(regex);
                handlers.add(new Handler(pattern, entry.getValue(), method));
                System.out.println("------   Mapping: " + regex + ", method:" + method);
            }
        }
    }

    // 6.运行阶段，等待请求
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
            String url = req.getRequestURI();
            String contextpath= req.getContextPath();
            url = url.replace(contextpath, "").replaceAll("/+", "/");
            System.out.println("进行请求....url：" + url);
        }catch (FileNotFoundException e){
            resp.getWriter().write("404 Not Found");
            return;
        }catch (Exception e){
            resp.getWriter().write("500 error \r\n\n" + Arrays.toString(e.getStackTrace()));
            return;
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        boolean jsonResult = false;
        // 获取适配器
        IHandlerAdapter handlerAdapter = defaultHandlerAdapter;
        Handler handler = handlerAdapter.getHandler(req, handlers);
        if (handler == null){
            throw new FileNotFoundException();
        }
        Object[] paramValues = handlerAdapter.hand(req, resp, handlers);
        Method method = handler.method;
        Object controller = handler.controller;
        String beanName = lowerFirstCase(method.getDeclaringClass().getSimpleName());

        //如果controller或这个方法有UVResponseBody修饰，返回json
        if (controller.getClass().isAnnotationPresent(ResponseBody.class) || method.isAnnotationPresent(ResponseBody.class)){
            jsonResult = true;
        }
        Object object = method.invoke(ioc.get(beanName), paramValues);
        if (jsonResult && object !=null){
            resp.getWriter().write(JSON.toJSONString(object));
        }else {
            // 返回视图
            doResolveView(object, req, resp);
        }
    }

    public void doResolveView(Object object, HttpServletRequest req, HttpServletResponse resp) throws Exception{
        // 视图前缀
        String prefix = props.getProperty("view.prefix");
        // 视图后缀
        String suffix = props.getProperty("view.suffix");

        MyModeAndView modeAndView = null;
        if (object instanceof MyModeAndView){
            modeAndView = (MyModeAndView) object;
        }else {
            modeAndView = new MyModeAndView(object.toString());
        }
        DefaultViewResolver viewResolver = new DefaultViewResolver(prefix, suffix);
        viewResolver.resolve(modeAndView, req, resp);
    }

    /**
     * 首字母小写
     * @param old
     * @return
     */
    private static String lowerFirstCase(String old){
        char [] chars = old.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
```

---
### Get,Post请求
```html
<html>
<head>
    <title>登陆</title>
</head>
<body>
<div>
    <h1>Get方式-欢迎登陆</h1>
    <form action="/test/login" method="get">
   <div>
       <label>用户名：</label>
       <input name="name" type="text"/>
   </div>
    <div>
        <label>用户名：</label>
        <input name="pwd" type="password"/>
    </div>
    <div>
        <label></label>
        <input type="submit" value="提交"/>
    </div>
    </form>
</div>
<hr>
<div>
    <h1>Post方式-欢迎登陆</h1>
    <form action="/test/login" method="post">
        <div>
            <label>用户名：</label>
            <input name="name" type="text"/>
        </div>
        <div>
            <label>用户名：</label>
            <input name="pwd" type="password"/>
        </div>
        <div>
            <label></label>
            <input type="submit" value="提交"/>
        </div>
    </form>
</div>
</body>
</html>
```

---

```html
 @RequestMapping(value = "/login", method = RequestMethod.POST)
    public MyModeAndView login(@RequestParam("name") String name, @RequestParam("pwd") String pwd){
        MyModeAndView modeAndView = new MyModeAndView();
        modeAndView.setViewName("index");
        modeAndView.addObject("name", name);
        return modeAndView;
    }
```
---
![avatar](https://raw.githubusercontent.com/chenxingxing6/springmvc/master/img/99.jpg)

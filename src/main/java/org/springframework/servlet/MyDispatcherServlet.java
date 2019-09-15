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
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
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
        }catch (Exception e){
            resp.getWriter().write("500 Exception, Details: \r\n\n" + e.getMessage());
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        boolean jsonResult = false;
        // 获取适配器
        IHandlerAdapter handlerAdapter = defaultHandlerAdapter;
        Object[] paramValues = handlerAdapter.hand(req, resp, handlers);
        Handler handler = handlerAdapter.getHandler(req, handlers);
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

        String indexView = "";
        MyModeAndView modeAndView = null;
        if (object instanceof MyModeAndView){
            modeAndView = (MyModeAndView) object;
            indexView = modeAndView.getViewName();
        }else {
            indexView = object.toString();
        }
        String view = (prefix + indexView + suffix).trim().replaceAll("/+", "/");
        System.out.println("视图解析器 " + view);
        DefaultViewResolver viewResolver = new DefaultViewResolver();
        modeAndView.setViewName(view);
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

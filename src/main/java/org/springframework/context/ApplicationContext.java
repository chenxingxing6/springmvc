package org.springframework.context;

import org.springframework.annotation.Autowired;
import org.springframework.annotation.Controller;
import org.springframework.annotation.Service;
import org.springframework.core.Handler;
import org.springframework.core.IHandlerAdapter;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: lanxinghua
 * Date: 2019/9/21 13:46
 * Desc: 上下文
 */
public class ApplicationContext {
    // 配置信息
    Properties props = new Properties();

    // 所有的类名，缓存
    private List<String> classNames = new ArrayList<>();

    // 所有视图模板
    private List<String> templateViewPaths = new ArrayList<>();

    // 实例化对象
    Map<String, Object> ioc = new ConcurrentHashMap<>();

    // Handler对应处理器适配器
    private Map<Handler, IHandlerAdapter> handlerAdapterMap = new HashMap<>();

    public Properties getConfig(){
        return props;
    }

    public Map<String, Object> getIoc(){
        return ioc;
    }

    public Map<Handler, IHandlerAdapter> getHandlerAdapterMap(){
        return handlerAdapterMap;
    }

    public void addHandlerAdapter(Handler handler, IHandlerAdapter handlerAdapter){
        handlerAdapterMap.put(handler, handlerAdapter);
    }

    public List<String> getTemplateViewPaths(){
        return templateViewPaths;
    }

    /**
     * 构造ApplicationContext
     * @param contextConfigLocation
     */
    public ApplicationContext(String contextConfigLocation){
        InputStream is = null;
        try {
            // 1.加载配置文件
            is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
            props.load(is);
            // 2.扫描包下class
            String packageName = props.getProperty("scanPackage");
            doScanner(packageName);
            // 3.实例化对象到IOC容器
            doInstance();
            // 4.依赖注入
            doAutowried();
            // 5.扫描视图模板
            String prefix = props.getProperty("view.prefix");
            doScannerTemplateView(prefix);
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

    /**
     * 进行递归扫描
     * @param packageName
     */
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
     * 进行递归扫描
     * @param templatePath
     */
    public void doScannerTemplateView(String templatePath){
        // 进行递归扫描
        templatePath = templatePath.startsWith("/") == true ? templatePath : "/" + templatePath;
        URL url = this.getClass().getClassLoader().getResource(templatePath);
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if (file.isDirectory()){
                doScannerTemplateView(templatePath + "/" + file.getName());
            }else {
                templateViewPaths.add(templatePath + "/" + file.getName());
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

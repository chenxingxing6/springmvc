package org.springframework.servlet;

import org.apache.commons.io.IOUtils;
import org.springframework.annotation.*;
import org.springframework.context.ApplicationContext;
import org.springframework.core.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: cxx
 * @Date: 2019/8/27 22:45
 * 模仿源码
 */
public class MyDispatcherServlet extends FrameworkServlet{
    // HandlerMapping  Controller
    List<Handler> handlerMapping = new ArrayList<>();
    // 视图对应的视图解析器
    private Map<String/*view名字 fileName*/, DefaultViewResolver/*视图解析器*/> viewResolverMap = new HashMap<>();

    public MyDispatcherServlet(){ }

    private ApplicationContext applicationContext;


    @Override
    protected void onRefresh(ApplicationContext context) {
        initStrategies(context);
    }

    // 初始化
    public void initStrategies(ApplicationContext context){
        System.out.println("------ My mvc is init start...... ------");
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
        System.out.println("------ My mvc is init end...... ------");
        applicationContext = context;
    }

    public void initMultipartResolver(ApplicationContext context){}
    public void initLocaleResolver(ApplicationContext context){}
    public void initThemeResolver(ApplicationContext context){}
    public void initHandlerExceptionResolvers(ApplicationContext context){}
    public void initRequestToViewNameTranslator(ApplicationContext context){}
    public void initFlashMapManager(ApplicationContext context){}

    public void initHandlerMappings(ApplicationContext context){
        Map<String,Object> ioc = context.getIoc();
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
                // url正则表达式匹配，Controller，Method
                handlerMapping.add(new Handler(pattern, entry.getValue(), method, requestMapping.method()));
                System.out.println("------   Mapping: " + regex + ", method:" + method);
            }
        }
    }

    public void initHandlerAdapters(ApplicationContext context){
        if (handlerMapping.isEmpty()){
            return;
        }
        Map<Handler, IHandlerAdapter> handlerAdapterMap = context.getHandlerAdapterMap();
        // 遍历所有的Handler,关联HandlerAdapter适配器(这里用默认适配器)
        for (Handler handler : handlerMapping) {
            handlerAdapterMap.put(handler, new DefaultHandlerAdapter());
        }
    }

    // 初始化模板
    public void initViewResolvers(ApplicationContext context){
        // 视图前缀
        String prefix = context.getConfig().getProperty("view.prefix");
        // 视图后缀
        String suffix = context.getConfig().getProperty("view.suffix");
        List<String> templateViewPaths = context.getTemplateViewPaths();
        if (templateViewPaths.isEmpty()){
            return;
        }
        for (String templateViewPath : templateViewPaths) {
            templateViewPath = templateViewPath.replace(".jsp", "");
            viewResolverMap.put(templateViewPath/* /index*/, new DefaultViewResolver(prefix, suffix, templateViewPath));
        }
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String url = req.getRequestURI();
            String contextpath= req.getContextPath();
            url = url.replace(contextpath, "").replaceAll("/+", "/");
            System.out.println("进行请求....url：" + url);
            if (url.equals("/")){
                resp.getWriter().write("手写SpringMvc....");
                return;
            }
            doDispatch(req, resp);
        }catch (FileNotFoundException e){
            resp.getWriter().write("404 Not Found");
            return;
        }catch (Exception e){
            resp.getWriter().write("500 error \r\n\n" + Arrays.toString(e.getStackTrace()));
            return;
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        // 通过处理器映射器HandlerMapping获取Handler(Controller)
        Handler handler = getHandler(req);
        if (handler == null){
            resp.getWriter().write("404 Not Found");
            return ;
        }
        if(applicationContext.getHandlerAdapterMap().isEmpty()){
            return;
        }

        // 对请求的方式进行校验
        String requestMethod = req.getMethod();
        if (handler.requestMethod.name().isEmpty() && requestMethod.equals(RequestMethod.GET.name())){
            handler.requestMethod = RequestMethod.GET;
        }else if (requestMethod.equalsIgnoreCase(handler.requestMethod.name())){
            // 方法匹配上 nothing
        }else {
            resp.getWriter().write("500 ERROR!  请求方式不对");
            return;
        }

        // 获取适配器
        IHandlerAdapter handlerAdapter = applicationContext.getHandlerAdapterMap().get(handler);
        MyModeAndView modeAndView = handlerAdapter.handler(req, resp, handler);

        if (modeAndView == null){
            resp.getWriter().write("404 Not Found");
            return;
        }
        if (!modeAndView.isFlag()){
            return;
        }

        // 视图解析
        String viewName = modeAndView.getViewName().startsWith("/") == true ? modeAndView.getViewName() : "/" + modeAndView.getViewName();
        viewName = viewName.contains("/jsp") == true ? viewName : applicationContext.getConfig().getProperty("view.prefix") + viewName;
        ViewResolver viewResolver = viewResolverMap.get(viewName);
        if (viewResolver == null){
            resp.getWriter().write("视图解析器没找到....");
            return;
        }
        View view = viewResolver.resolve(modeAndView, Locale.CANADA);

        // 渲染视图
        IOUtils.write(view.getData(), resp.getOutputStream(), "UTF-8");
        return;
    }


    /**
     * 获取Handler
     */
    public Handler getHandler(HttpServletRequest req){
        String url = req.getRequestURI();
        String contextpath= req.getContextPath();
        url = url.replace(contextpath, "").replaceAll("/+", "/");
        for (Handler handler : handlerMapping) {
            try {
                Matcher matcher = handler.pattern.matcher(url);
                // 如果没匹配上，继续下个匹配
                if (!matcher.matches()){
                    continue;
                }
                return handler;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

}

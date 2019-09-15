package org.springframework.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: cxx
 * @Date: 2019/8/27 22:45
 */
public class MyDispatcherServlet extends HttpServlet{
    private List<String> classNames = new ArrayList<>();
    Map<String, Object> beans = new ConcurrentHashMap<>();
    Map<String, Object> handlerMap = new HashMap<>();
    Properties props = null;
    private static final String HANDLER_ADAPTER_PACKAGE = "org.org.springframework.handleradapter";

    public MyDispatcherServlet(){

    }

    // 初始化
    @Override
    public void init() throws ServletException {
        System.out.println("My mvc is init ......");
        // 1.基本包进行扫描，获取包下及子包下所有类

        // 2.扫描处理的类进行实例化

        // 3.依赖注入

        // 4.建立path和method的映射关系
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }


}

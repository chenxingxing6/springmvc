package org.springframework.servlet;

import org.springframework.context.ApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * User: lanxinghua
 * Date: 2019/9/21 14:06
 * Desc:
 */
public abstract class FrameworkServlet extends HttpServlet {
    @Override
    public void init(ServletConfig config) throws ServletException {
        // 初始化上下文内容
        String location = config.getInitParameter("contextConfigLocation").split(":")[1];
        ApplicationContext context = new ApplicationContext(location);
        onRefresh(context);
    }

    protected void onRefresh(ApplicationContext context) {

    }
}

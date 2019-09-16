package org.springframework.web.filter;

import javax.servlet.*;
import java.io.IOException;

/**
 * @Author: cxx
 * @Date: 2019/9/15 23:56
 */
public class CharacterEncodingFilter implements Filter{
    // 编码格式
    private String encoding;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("------ filter init .......");
        encoding = filterConfig.getInitParameter("encoding");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("------ 过滤器处理编码问题......" + encoding);
        servletRequest.setCharacterEncoding(encoding);
        servletResponse.setCharacterEncoding(encoding);
        servletResponse.setContentType("text/html");
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}

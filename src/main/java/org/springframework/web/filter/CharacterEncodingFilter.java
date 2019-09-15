package org.springframework.web.filter;

import javax.servlet.*;
import java.io.IOException;

/**
 * @Author: cxx
 * @Date: 2019/9/15 23:56
 */
public class CharacterEncodingFilter implements Filter{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("filter ......");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

    }

    @Override
    public void destroy() {

    }
}

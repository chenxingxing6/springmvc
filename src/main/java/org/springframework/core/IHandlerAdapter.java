package org.springframework.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author: cxx
 * @Date: 2019/9/15 22:50
 */
public interface IHandlerAdapter {

    public MyModeAndView handler(HttpServletRequest req, HttpServletResponse resp, Handler handler) throws Exception;

}

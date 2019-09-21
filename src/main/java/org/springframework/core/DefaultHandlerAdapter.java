package org.springframework.core;

import com.alibaba.fastjson.JSON;
import org.springframework.annotation.ResponseBody;
import org.springframework.annotation.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @Author: cxx
 * @Date: 2019/9/15 22:56
 */
public class DefaultHandlerAdapter implements IHandlerAdapter {
    @Override
    public MyModeAndView handler(HttpServletRequest req, HttpServletResponse resp, Handler handler) throws Exception {
        // 获取方法参数列表
        Class<?>[] parameterTypes = handler.method.getParameterTypes();
        Object[] paramValues = new Object[parameterTypes.length];
        Map<String, String[]> paramsMap = req.getParameterMap();

        // 参数列表
        for (Map.Entry<String, String[]> param : paramsMap.entrySet()) {
            String value = parseParamValue(param.getValue());
            if (!handler.paramIndexmapping.containsKey(param.getKey())){
                continue;
            }
            int index = handler.paramIndexmapping.get(param.getKey());
            paramValues[index] = convert(parameterTypes[index], value);
        }

        // 设置方法中的request 和 response
        Integer reqIndex = handler.paramIndexmapping.get(HttpServletRequest.class.getName());
        if (reqIndex !=null){
            paramValues[reqIndex] = req;
        }

        Integer respIndex = handler.paramIndexmapping.get(HttpServletResponse.class.getName());
        if (respIndex != null){
            paramValues[respIndex] = resp;
        }

        Object result = handler.method.invoke(handler.controller, paramValues);
        if (result instanceof MyModeAndView){
            return (MyModeAndView) result;
        }
        //如果controller或这个方法有ResponseBody修饰，返回json
        if (handler.controller.getClass().isAnnotationPresent(ResponseBody.class) || handler.method.isAnnotationPresent(ResponseBody.class)){
            resp.setContentType("application/json; charset=utf-8");
            Class cls = result.getClass();
            if (cls == String.class || cls == Integer.class || cls == Long.class || cls == int.class || cls == long.class){
                resp.getWriter().print(result);
            }else {
                resp.getWriter().print(JSON.toJSONString(result));
            }
            MyModeAndView modeAndView = new MyModeAndView();
            modeAndView.setFlag(false);
            return modeAndView;
        }else {
            return new MyModeAndView("/" + result.toString());
        }
    }

    private String parseParamValue(String[] params){
        return Arrays.toString(params)
                .replaceAll("\\[|\\]", "")
                .replaceAll("\\s", ",");
    }

    private Object convert(Class clazz, String value){
        if (clazz == Integer.class){
            return Integer.valueOf(value);
        }
        if (clazz == String.class){
            return String.valueOf(value);
        }
        if (clazz == Long.class){
            return Long.valueOf(value);
        }
        return value;
    }
}

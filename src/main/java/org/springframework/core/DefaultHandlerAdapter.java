package org.springframework.core;

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
@Service
public class DefaultHandlerAdapter implements IHandlerAdapter {
    @Override
    public Object[] hand(HttpServletRequest req, HttpServletResponse resp, List<Handler> handlers) throws Exception{
        Handler handler = getHandler(req, handlers);
        if (handler == null){
            resp.getWriter().write("404 Not Found!");
            return null;
        }

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
        return paramValues;
    }

    /**
     * 获取Handler
     * @param req
     * @param handlers
     * @return
     */
    public Handler getHandler(HttpServletRequest req, List<Handler> handlers){
        if (handlers.isEmpty()){
            return null;
        }
        String url = req.getRequestURI();
        String contextpath= req.getContextPath();
        url = url.replace(contextpath, "").replaceAll("/+", "/");
        for (Handler handler : handlers) {
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
        return value;
    }
}

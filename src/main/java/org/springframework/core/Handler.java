package org.springframework.core;

import org.springframework.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @Author: cxx
 * @Date: 2019/9/15 22:54
 * 记录Controller与Requestmapping和Method对应关系
 */
public class Handler {
    public Object controller;
    public Method method;
    public Pattern pattern;
    public Map<String, Integer> paramIndexmapping;

    public Handler(Pattern pattern, Object controller, Method method) {
        this.pattern = pattern;
        this.controller = controller;
        this.method = method;
        paramIndexmapping = new HashMap<>();
        putParamIndexMapping(method);

    }

    private void putParamIndexMapping(Method method){
        // 提取方法中加了注解的参数
        Annotation[][] pa = method.getParameterAnnotations();
        for (int i = 0; i < pa.length; i++) {
            for (Annotation a : pa[i]) {
                if (a instanceof RequestParam){
                    String paramName = ((RequestParam) a).value();
                    if (!"".equals(paramName.trim())){
                        paramIndexmapping.put(paramName, i);
                    }
                }
            }
        }
        // 提取方法中的request和response
        Class<?> [] paramsTypes = method.getParameterTypes();
        for (int i = 0; i < paramsTypes.length; i++) {
            Class type = paramsTypes[i];
            if (type == HttpServletRequest.class || type == HttpServletResponse.class){
                paramIndexmapping.put(type.getName(), i);
            }
        }
    }
}

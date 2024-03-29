package org.springframework.core;

import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: cxx
 * @Date: 2019/9/16 0:39
 * 默认视图解析器
 */
public class DefaultViewResolver implements ViewResolver{
    // 用来缓存页面，这样就不用每次都读模版文件，减少IO
    Map<String/*视图路径*/, String> cache = new HashMap<>();

    // 是否要缓存模版
    boolean isCached = false;

    private String prefix = "/jsp/";

    private String suffix = ".jsp";

    private String filePath;

    public DefaultViewResolver(){

    }

    public DefaultViewResolver(String prefix, String suffix, String filePath){
        this.prefix = prefix;
        this.suffix = suffix;
        this.filePath = filePath;
    }

    @Override
    public View resolve(MyModeAndView modeAndView, Locale locale) throws Exception {
        return new View(getHtml(modeAndView, locale));
    }

    private String getHtml(MyModeAndView modeAndView, Locale locale) {
        String viewName = modeAndView.getViewName().startsWith("/") == true ? modeAndView.getViewName() : "/" + modeAndView.getViewName();
        // 缓存中取
        String view = (prefix + viewName + suffix).trim().replaceAll("/+", "/");
        String s = cache.get(view);
        if (null == s) {
            try {
                // 获取模版文件
                InputStream is = this.getClass().getClassLoader().getResourceAsStream(view);
                // 读取为字符串
                s = IOUtils.toString(is, "UTF-8");
            } catch (FileNotFoundException e) {
                throw new NullPointerException(String.format("视图[%s]不存在", view));
            } catch (Exception e) {
                e.printStackTrace();
            }
            s = fillValue(s, modeAndView.getMap());
            if (isCached) {
                cache.put(view, s);
            }
        }
        return s;
    }

    private String fillValue(String s, Map<String, Object> data) {
        Matcher matcher = Pattern.compile("\\$\\{(.+?)\\}").matcher(s);
        while (matcher.find()){
            String key = matcher.group(1);// 键名
            String value = (String) data.get(key);// 键值
            s = s.replace(String.format("${%s}", key), value);
        }
        return s;
    }
}

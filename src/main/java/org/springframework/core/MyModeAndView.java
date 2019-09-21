package org.springframework.core;

import java.util.HashMap;
import java.util.Map;

/**
 * User: lanxinghua
 * Date: 2019/9/15 16:22
 * Desc:
 */
public class MyModeAndView {
    private String viewName;
    private Map<String, Object> map = new HashMap<>();
    private boolean flag = true;

    public MyModeAndView(){

    }

    public MyModeAndView(String viewName){
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public void addObject(String key, Object object){
        map.put(key, object);
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public boolean isFlag() {
        return flag;
    }
}

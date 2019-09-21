package org.springframework.core;

import java.io.Serializable;

/**
 * User: lanxinghua
 * Date: 2019/9/21 15:10
 * Desc:
 */
public class View implements Serializable {
    public String data;

    public View(String data){
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}

package org.springframework.core;

import java.util.Locale;

/**
 * User: lanxinghua
 * Date: 2019/9/21 15:09
 * Desc:
 */
public interface ViewResolver {
    View resolve(MyModeAndView modeAndView, Locale locale) throws Exception;
}

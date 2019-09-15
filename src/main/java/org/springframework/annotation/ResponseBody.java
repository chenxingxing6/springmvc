package org.springframework.annotation;

import java.lang.annotation.*;

/**
 * @Author: cxx
 * @Date: 2019/9/15 22:57
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.METHOD)
public @interface ResponseBody {
    String value() default "";
}

package org.springframework.annotation;

import java.lang.annotation.*;

/**
 * @Author: cxx
 * @Date: 2019/8/27 22:40
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
    String value() default "";
}

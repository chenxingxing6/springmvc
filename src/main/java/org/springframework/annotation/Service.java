package org.springframework.annotation;

import java.lang.annotation.*;

/**
 * @Author: cxx
 * @Date: 2019/8/27 22:37
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Service {
    String value() default "";
}

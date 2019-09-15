package org.springframework.annotation;

import java.lang.annotation.*;

/**
 * User: lanxinghua
 * Date: 2019/9/15 11:36
 * Desc:
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface RequestParam {
    String value() default "";
}

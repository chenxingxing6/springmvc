package org.springframework.annotation;

import java.lang.annotation.*;

/**
 * User: lanxinghua
 * Date: 2019/9/15 11:35
 * Desc:
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
    String value() default "";
}

package com.pt.common.rpc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 被注解的字段将会放在http header中
 * @author hechengchen
 * @date 2018/4/16 下午7:47
 */

@Target(PARAMETER)
@Documented
@Retention(RUNTIME)
public @interface HttpHeaderParam {

    String value() default "";

}

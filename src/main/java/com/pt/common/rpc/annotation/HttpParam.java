package com.pt.common.rpc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 注解的value将作为参数的key
 * @author hechengchen
 * @date 2018/6/1 上午10:52
 */
@Target(PARAMETER)
@Documented
@Retention(RUNTIME)
public @interface HttpParam {

    String value();

}

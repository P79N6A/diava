package com.pt.common.es;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author hechengchen
 * @date 2018/5/24 下午5:59
 */

@Target(ElementType.FIELD)
@Documented
@Retention(RUNTIME)
public @interface EsField {

    String value();

}

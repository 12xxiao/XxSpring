package com.xx.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)//该注解生效的时间// 保留到运行时
@Target(ElementType.TYPE)// 只能用在类上
//指定Bean 的作用域
public @interface Scope {

    String value() default "";
}

package com.xx.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)//该注解生效的时间// 保留到运行时
@Target(ElementType.TYPE)// 只能用在类上

//声明一个注解类型 @ComponentScan
// @interface:用来创建自定义注解的关键字
public @interface ComponentScan {
    //1.参数value的默认值是空字符串，指定扫描路径
    //2.如果参数名是 value，且只有这一个参数，那么在使用注解时可以省略 value= 这一部分
        //@MyAnnotation("xxx")   // value() 默认规则
        //@MyAnnotation(value="xxx")  // 等价
    String value() default "";
}

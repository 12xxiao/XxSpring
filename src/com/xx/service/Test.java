package com.xx.service;

import com.xx.spring.XxApplicationContext;


public class Test {
    public static void main(String[] args)  {
        //用spring的时候需要构造一个spring容器
        //里面要么spring.xml，要么传配置类
        //spring根据配置（AppConfig.class）去扫描，等等
        XxApplicationContext applicationContext = new XxApplicationContext(AppConfig.class);

//        UserService userService = (UserService) applicationContext.getBean("userService");
        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("userService"));
    }
}

package com.xx.service;

import com.xx.spring.BeanPostProcessor;
import com.xx.spring.Component;

@Component
public class XxBeanPostProcessor implements BeanPostProcessor {

    @Override
    public void postProcessBeforeInitialization(String beanName, Object bean) {
        if (beanName.equals("userService")) {
            System.out.println("PostProcessBefore");
        }
    }

    @Override
    public void postProcessAfterInitialization(String beanName, Object bean) {
        if (beanName.equals("userService")) {
            System.out.println("PostProcessAfter");
        }
    }
}

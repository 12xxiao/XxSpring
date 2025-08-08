package com.xx.service;

import com.xx.spring.Autowired;
import com.xx.spring.Component;
import com.xx.spring.Scope;

@Component
public class UserService {

    @Autowired
    private OrderService orderService;

    public void test(){
        System.out.println(orderService);
    }
}

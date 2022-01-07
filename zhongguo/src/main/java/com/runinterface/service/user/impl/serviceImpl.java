package com.runinterface.service.user.impl;

import com.runinterface.service.user.service;
import org.springframework.stereotype.Service;

@Service("userservice")
public class serviceImpl implements service {

    public void say() {
        System.out.println("say:hello");
    }
}

package com.zhongguo.com.service.serviceImpl;

import com.zhongguo.com.service.UserService;
import org.springframework.stereotype.Service;

@Service("userServiceImpl")
public class UserServiceImpl implements UserService {
    @Override
    public void Say() {
        System.out.println("手动加载");

    }
}

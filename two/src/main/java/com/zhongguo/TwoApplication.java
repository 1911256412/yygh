package com.zhongguo;

import com.zhongguo.com.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TwoApplication {

    public static void main(String[] args) {
        //获取容器对象
        ConfigurableApplicationContext ctx = SpringApplication.run(TwoApplication.class, args);
        UserService service = (UserService) ctx.getBean("userServiceImpl");
        service.Say();

    }
}

package com.runinterface;

import com.runinterface.service.user.service;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.Resource;

@SpringBootApplication
public class ZhongguoApplication implements CommandLineRunner {

    @Resource
    private service service;

    public static void main(String[] args) {
        System.out.println("创建容器之前");
        ConfigurableApplicationContext caf = SpringApplication.run(ZhongguoApplication.class, args);
        service s = (service) caf.getBean("userservice");
        s.say();
        System.out.println("创建容器之后");
    }

    public void run(String... args) throws Exception {
        System.out.println("实现run方法");
//        service.say();
//        System.out.println("实现run方法之后");
    }
}

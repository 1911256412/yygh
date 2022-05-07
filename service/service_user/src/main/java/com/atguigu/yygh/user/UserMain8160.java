package com.atguigu.yygh.user;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.atguigu")
@EnableFeignClients(basePackages = "com.atguigu.client")
@SpringBootApplication
@EnableDiscoveryClient
public class UserMain8160 {
    public static void main(String[] args) {
        SpringApplication.run(UserMain8160.class,args);
    }
}

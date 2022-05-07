package com.atguigu.statistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableFeignClients(basePackages = "com.atguigu")
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.atguigu")
public class StatisticsMain {
    public static void main(String[] args) {
        SpringApplication.run(StatisticsMain.class,args);
    }
}

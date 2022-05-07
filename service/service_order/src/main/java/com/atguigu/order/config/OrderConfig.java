package com.atguigu.order.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@MapperScan("com.atguigu.order.mapper")
public class OrderConfig {

}

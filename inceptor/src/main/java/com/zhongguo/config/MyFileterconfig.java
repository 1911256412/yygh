package com.zhongguo.config;

import com.zhongguo.filter.Myfilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyFileterconfig {
  //  @Bean
//    public FilterRegistrationBean filterRegistrationBean(){
//        FilterRegistrationBean bean =new FilterRegistrationBean();
//        //注册过滤器
//        bean.setFilter(new Myfilter());
//        //设置地址
//        bean.addUrlPatterns("/test/*");
//        return bean ;
//    }
}

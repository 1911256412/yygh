package com.zhongguo.config;

import com.zhongguo.servlet.Myservlet;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;

@Configuration
public class servletConfig {
    @Bean
    public ServletRegistrationBean servletRegistrationBean(){
      //  ServletRegistrationBean bean =new ServletRegistrationBean(new Myservlet(),"/myservlet");
        ServletRegistrationBean bean =new ServletRegistrationBean();
        bean.addUrlMappings("/myservlet1");
        bean.setServlet(new Myservlet());
        return bean;
    }
    //注册Filter
//   @Bean
//    public FilterRegistrationBean filterRegistrationBean(){
//        CharacterEncodingFilter filter=new CharacterEncodingFilter();
//        //使用框架中的过滤器
//        FilterRegistrationBean reg =new FilterRegistrationBean();
//        //设置字符集
//        filter.setEncoding("utf-8");
//        //强制request 和response 使用encoding的值
//        filter.setForceEncoding(true);
//        reg.setFilter(filter);
//        //设置过滤器路径
//        reg.addUrlPatterns("/*");
//        return reg;
//    }
}

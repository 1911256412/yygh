package com.zhongguo.config;

import com.zhongguo.Interceptor.Myhandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration//相当于mvc配置文件
public class MyappConfig implements WebMvcConfigurer {
    //添加拦截器
    public void addInterceptors(InterceptorRegistry registry) {
        //把自己的建的拦截器拿过来
        Myhandler myhandler=new Myhandler();
        String path[]={"/user/**"};
        String exclude[]={"/user/login"};
        registry.addInterceptor(myhandler).addPathPatterns().excludePathPatterns();
    }
}

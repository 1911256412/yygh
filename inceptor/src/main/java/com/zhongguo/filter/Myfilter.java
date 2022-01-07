package com.zhongguo.filter;


import javax.servlet.*;
import java.io.IOException;

public class Myfilter implements Filter {
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        System.out.println("执行了过滤器+test路径下");
        filterChain.doFilter(servletRequest,servletResponse);//把请求传递过去
    }
}

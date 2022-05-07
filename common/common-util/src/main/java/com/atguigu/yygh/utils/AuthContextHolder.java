package com.atguigu.yygh.utils;

import javax.servlet.http.HttpServletRequest;

public class AuthContextHolder {
    //获取用户id
    public static Long getUserId(HttpServletRequest request) {
        Long userId = JwtHelper.getUserId(request.getHeader("token"));
        return userId;
    }

    public static String getName(HttpServletRequest request) {
        String name = JwtHelper.getUserName(request.getHeader("token"));
        return name;
    }
}

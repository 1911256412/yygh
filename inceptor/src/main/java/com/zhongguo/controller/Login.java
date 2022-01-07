package com.zhongguo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class Login {

    @RequestMapping("/user/u")
    @ResponseBody
    public String login(){
        return "/user/u";
    }

    @RequestMapping("/user/login")
    @ResponseBody
    public String login1(){
        return "/user/login";
    }

    @RequestMapping("/test/login")
    @ResponseBody
    public String login2(){
        return "test/login";
    }

}

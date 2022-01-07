package com.zhongguo.com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

@Controller
public class jspController {

    @RequestMapping("/testjsp")
    public String testjsp (){

        return "index";
    }
}

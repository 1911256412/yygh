package com.zhongguo.com.controller;

import com.zhongguo.com.vo.School;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
public class firset {

    @Value("${school.name}")
    private  String schoolname ;
    @Value("${websit}")
    private String web ;

    @Resource
    private School school;
    @RequestMapping("/hello")
    @ResponseBody
    public String springboot(){
//        return "学校名称"+schoolname+"网站"+web;
    return "姓名"+school;
    }
}

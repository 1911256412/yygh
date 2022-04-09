package com.he.mapper.controller;

import com.he.mapper.pojo.Student;
import com.he.mapper.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
public class StudentController {

    @Resource
    private StudentService studentservice;

    @RequestMapping("/user")
    @ResponseBody
    public String query(Integer id) {
        System.out.println(studentservice.selectByid(id));
        System.out.println(id);
        return studentservice.selectByid(id).toString();

    }

    @RequestMapping("/user1")
    @ResponseBody
    public String add(Student student) {
        int rows = studentservice.addStudent(student);
        return "添加" + rows;

    }




}

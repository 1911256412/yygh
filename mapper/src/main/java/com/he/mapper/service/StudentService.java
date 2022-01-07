package com.he.mapper.service;

import com.he.mapper.pojo.Student;

public interface StudentService {
    Student selectByid (Integer id );
    int addStudent(Student student);
}

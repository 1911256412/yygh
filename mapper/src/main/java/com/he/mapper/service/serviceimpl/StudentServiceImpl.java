package com.he.mapper.service.serviceimpl;


import com.he.mapper.mapper.StudentMapper;
import com.he.mapper.pojo.Student;
import com.he.mapper.service.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class StudentServiceImpl implements StudentService {

    @Resource
    private StudentMapper mapper;

    public Student selectByid(Integer id) {
        return mapper.selectByPrimaryKey(id);
    }

    /**
     * @Transactional 事务
     * @param student
     * @return
     */
    @Transactional
    public int addStudent(Student student) {
        int rows =mapper.insert(student);

       // int i=10/0;

        return rows;
    }
}

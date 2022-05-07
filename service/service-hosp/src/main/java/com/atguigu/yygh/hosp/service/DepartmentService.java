package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    void save(Map<String, Object> map);

    Page<Department> selectByPage(int page, int limit, DepartmentQueryVo departmentVo);

    void remove(String hoscode, String depcode);

    List<DepartmentVo> selectDeparment(String hoscode);

    String  getDepartmentByHoscodeAndDepcode(String depcode, String hoscode);

    Department getDepartment(String hoscode, String depcode);
}

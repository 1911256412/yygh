package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Resource
    private DepartmentRepository departmentRepository;

    @Override
    public void save(Map<String, Object> map) {
        String s = JSONObject.toJSONString(map);
        Department department = JSONObject.parseObject(s, Department.class);
        Department tardepartment=departmentRepository.getDepartmentByHoscodeAndDepcode(department.getHoscode(),department.getDepcode());
        if(tardepartment!=null){
            //进行修改
            tardepartment.setCreateTime(department.getCreateTime());
            tardepartment.setUpdateTime(new Date());
            tardepartment.setIsDeleted(0);
            departmentRepository.save(tardepartment);
        }else {
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }
    }

    @Override
    public Page<Department> selectByPage(int page, int limit, DepartmentQueryVo departmentVo) {

        Pageable pageable= PageRequest.of(page-1,limit);
        Department department=new Department();
        BeanUtils.copyProperties(departmentVo,department);
        department.setIsDeleted(0);
        ExampleMatcher matcher=ExampleMatcher.matching()//构建对象
        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)//改变字符串匹配方式模糊查询
        .withIgnoreCase(true);//改变默认大小写方式
        //创建实例
        Example<Department> example=Example.of(department,matcher);
        Page<Department> all = departmentRepository.findAll(example, pageable);
        return all;
    }

    @Override
    public void remove(String hoscode, String depcode) {
        //根据医院编号和科室信号查询医院信息
        Department departmentByHoscodeAndDepcode = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if(departmentByHoscodeAndDepcode!=null){
            departmentRepository.delete(departmentByHoscodeAndDepcode);
        }
    }

    @Override
    public List<DepartmentVo> selectDeparment(String hoscode) {

        Department department=new Department();
        department.setHoscode(hoscode);
        Example<Department> example=Example.of(department);
        List<Department> departmentList = departmentRepository.findAll(example);
//        System.out.println("分组之前的数据 ------------------");
//        departmentList.stream().forEach(item->{
//            System.out.println("分组之前-----"+item);
//        });
        //把所有数据按照大科室编号分组
        Map<String, List<Department>> map = departmentList.stream().collect(Collectors.groupingBy(Department::getBigcode));
        //最终的树形信息的集合
        List<DepartmentVo> departmentVoList=new ArrayList<>();
        //遍历map集合
        //根据key和value的关系来遍历 ，entryset,通过for循环
        for(Map.Entry<String, List<Department>> entry :map.entrySet()){
            //键就是大科室编号
            String bigCode=entry.getKey();
            //值就是对应的所有科室
          //  System.out.println("大科室code" +bigCode);
            List<Department> departmentsAll = entry.getValue();
         //   System.out.println("根据大科室键对应的科室值"+ departmentsAll.toString());

            //封装大科室
            DepartmentVo departmentVo=new DepartmentVo();
            departmentVo.setDepcode(bigCode);
            departmentVo.setDepname(departmentsAll.get(0).getBigname());

            //所有小科室集合
            List<DepartmentVo> childList=new ArrayList<>();

            //封装小科室
            for(Department department1:departmentsAll){
                DepartmentVo departmentVo1=new DepartmentVo();
                departmentVo1.setDepname(department1.getDepname());
                departmentVo1.setDepcode(department1.getDepcode());
                childList.add(departmentVo1);
            }
            //把所有小科室集合添加到大科室集合中
            departmentVo.setChildren(childList);
            //把所有大科室添加到最终的树形结构中
            departmentVoList.add(departmentVo);
        }


        return departmentVoList;
    }

    @Override
    public String getDepartmentByHoscodeAndDepcode(String depcode, String hoscode) {

            Department department=departmentRepository.getDeparmentByDepcodeAndHoscode(depcode,hoscode);
            if(department.getDepname()!=null){
                return department.getDepname();
            }
            return null;

    }

    @Override
    public Department getDepartment(String hoscode, String depcode) {
        Department deparmentByDepcodeAndHoscode = departmentRepository.getDeparmentByDepcodeAndHoscode(depcode, hoscode);
        return deparmentByDepcodeAndHoscode;
    }
}

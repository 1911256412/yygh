package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.client.DictFeiginClient;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class HospitalServiceImpl implements HospitalService {
    @Resource
    private HospitalRepository hospitalRepository;

    @Resource
    private DictFeiginClient dictFeiginClient;

    @Override
    public void save(Map<String, Object> map) {
        //需要把map集合转换为对象
        String s = JSONObject.toJSONString(map);
        Hospital hospital = JSONObject.parseObject(s, Hospital.class);
        //判断mongo是否存在数据,通过hoscode来查询
        Hospital hospital1Exit = hospitalRepository.getHospitalByHoscode(hospital.getHoscode());

        //如果不存在进行添加
        //如果在医院中接过来的数据在mongo中不存在，进行修改
        if (hospital1Exit != null) {
            hospital.setStatus(hospital.getStatus());
            hospital.setCreateTime(hospital.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        } else {
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }
    }

    @Override
    public Hospital getByHocode(String hoscode) {
        Hospital hospitalByHoscode = hospitalRepository.getHospitalByHoscode(hoscode);

        return hospitalByHoscode;
    }

    @Override
    public Page<Hospital> selectByPage(Integer current, Integer limit, HospitalQueryVo hospitalQueryVo) {
        //创建pageable对象
        Pageable pageable = PageRequest.of(current - 1, limit);
        //创建条件匹配器
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Hospital hospital = new Hospital();
//        hospital.setIsDeleted(0);
//        hospital.setHostype(hospitalQueryVo.getHostype());
//        hospital.setHoscode(hospitalQueryVo.getHoscode());
//        hospital.setStatus(hospitalQueryVo.getStatus());
//        hospital.setCreateTime(new Date());
//        hospital.setUpdateTime(new Date());
//        hospital.setHosname(hospitalQueryVo.getHosname());
//        hospital.setProvinceCode( hospitalQueryVo.getProvinceCode());
        BeanUtils.copyProperties(hospitalQueryVo, hospital);
       // BeanUtils.copyProperties(hospitalQueryVo, hospital);
        //创建实例
        Example<Hospital> example = Example.of(hospital, matcher);
        Page<Hospital> hospitalPage = hospitalRepository.findAll(example, pageable);
        List<Hospital> content = hospitalPage.getContent();
        content.stream().forEach(item -> {
            this.setHospital(item);

        });

        return hospitalPage;
    }

    @Override
    //修改mongoDB数据库中状态
    public void updateStatus(String id, String status)
    {
        Hospital hospital = hospitalRepository.findById(id).get();
        hospital.setStatus(Integer.parseInt(status));
        hospital.setUpdateTime(new Date());
        hospitalRepository.save(hospital);
    }


    @Override
    public Map<String, Object> show(String id) {
        Map<String, Object> result = new HashMap<>();
      ;
        Hospital hospital = this.setHospital( hospitalRepository.findById(id).get());
        result.put("hospital", hospital);

//单独处理更直观
        result.put("bookingRule", hospital.getBookingRule());
//不需要重复返回
        hospital.setBookingRule(null);
        return result;
    }

    @Override
    public String selectByhospcode(String hoscode) {
        Hospital hospitalByHoscode = hospitalRepository.getHospitalByHoscode(hoscode);

        return hospitalByHoscode.getHosname();
    }

    @Override
    public List<Hospital> selectByhosname(String hosname) {

        List<Hospital> list=hospitalRepository.getHospitalByHosname(hosname);
        return list;
    }

    @Override
    public Map<String, Object> item(String hoscode) {

        Hospital hospital = this.setHospital(this.getByHocode(hoscode));
        Map <String ,Object> result=new HashMap<>();
        result.put("hospital", hospital);
        //预约规则
        result.put("bookingRule", hospital.getBookingRule());
        //不需要重复返回
        hospital.setBookingRule(null);
        return result;

    }


    private Hospital setHospital(Hospital item) {
        //查询医院名称根据mongoDB中的
        String hostypeString = dictFeiginClient.getName("Hostype", item.getHostype());
        //查询省份
        String provice = dictFeiginClient.getName(item.getProvinceCode());
        String city = dictFeiginClient.getName(item.getCityCode());
        String district = dictFeiginClient.getName(item.getDistrictCode());
        item.getParam().put("fullAddress", provice + city + district);
        item.getParam().put("hostypeString", hostypeString);

        return item;
    }
}

package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface HospitalService  {
    void save(Map<String, Object> map);

    Hospital getByHocode(String hoscode);

    Page<Hospital> selectByPage(Integer current, Integer limit, HospitalQueryVo hospitalQueryVo);

    void updateStatus(String id, String status);

    Object show(String id);

    String selectByhospcode(String hoscode);


    List<Hospital> selectByhosname(String hosname);

    //根据医院编号获取医院预约挂号详情
    Map<String, Object> item(String hoscode);
}

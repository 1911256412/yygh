package com.atguigu.yygh.hosp.service.impl;

import com.atguigu.yygh.hosp.mapper.HospitalSetMapper;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper, HospitalSet>implements HospitalSetService {


    @Override
    public String selectkey(String hoscode) {
        LambdaQueryWrapper<HospitalSet> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(HospitalSet::getHoscode,hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        String signkey=hospitalSet.getSignKey();

        return signkey;
    }
}

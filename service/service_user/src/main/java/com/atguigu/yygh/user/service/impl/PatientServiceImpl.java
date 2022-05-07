package com.atguigu.yygh.user.service.impl;

import com.atguigu.client.DictFeiginClient;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.mapper.PatientMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper,Patient> implements PatientService {

    @Resource
    private DictFeiginClient dictFeiginClient;

    @Override
    public List<Patient> findAllPatient(Long userId) {
        LambdaQueryWrapper<Patient> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Patient::getUserId,userId);
        List<Patient> patients = baseMapper.selectList(wrapper);
        patients.stream().forEach(item->{
            this.Packeage(item);
        });
        return patients;
    }
    private Patient Packeage(Patient patient) {
        //根据证件类型编码，获取证件类型具体指
        String certificatesTypeString =
                dictFeiginClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(), patient.getCertificatesType());//联系人证件
        //联系人证件类型
        String contactsCertificatesTypeString =
                dictFeiginClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(),patient.getContactsCertificatesType());
        //省
        String provinceString = dictFeiginClient.getName(patient.getProvinceCode());
        //市
        String cityString = dictFeiginClient.getName(patient.getCityCode());
        //区
        String districtString = dictFeiginClient.getName(patient.getDistrictCode());
        patient.getParam().put("certificatesTypeString", certificatesTypeString);
        patient.getParam().put("contactsCertificatesTypeString", contactsCertificatesTypeString);
        patient.getParam().put("provinceString", provinceString);
        patient.getParam().put("cityString", cityString);
        patient.getParam().put("districtString", districtString);
        patient.getParam().put("fullAddress", provinceString + cityString + districtString + patient.getAddress());
        return patient;
    }

    @Override
    public boolean savePatient(Patient patient, Long userId) {
        patient.setUserId(userId);
        int insert = baseMapper.insert(patient);

        return true;
    }
}

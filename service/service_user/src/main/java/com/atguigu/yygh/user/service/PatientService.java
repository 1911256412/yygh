package com.atguigu.yygh.user.service;


import com.atguigu.yygh.model.user.Patient;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PatientService extends IService<Patient> {
    List<Patient> findAllPatient(Long userId);

    boolean savePatient(Patient patient, Long userId);
}

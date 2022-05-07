package com.atguigu.yygh.user.api;

import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.result.Result;
import com.atguigu.yygh.user.service.PatientService;
import com.atguigu.yygh.utils.AuthContextHolder;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/user/patient")
public class PatientController {

    @Resource
    private PatientService patientService;
    @ApiOperation("根据用户id查询就诊人列表")
    @GetMapping("auth/findAll")
    public Result findAll(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        List<Patient> list =patientService.findAllPatient(userId);
        return Result.ok(list);
    }
    @ApiOperation("添加就诊人")
    //添加就诊人
    @PostMapping("auth/save")
    public Result save(HttpServletRequest request, @RequestBody Patient patient){
        Long userId = AuthContextHolder.getUserId(request);
        boolean b =patientService.savePatient(patient,userId);
        if(b){
            return Result.ok();
        }
        return Result.fail();
    }
    @ApiOperation("根据就诊人id查询 ")
    //根据id获取就诊人信息
    @GetMapping("auth/get/{patientId}")
    public Result selectPatient(@PathVariable  Integer patientId) {
        Patient byId = patientService.getById(patientId);
        return Result.ok(byId);
    }
    @ApiOperation("修改就诊人信息 ")
    //修改就诊人
    @PostMapping("auth/update")
    public Result updatePatient(@RequestBody Patient patient) {
        boolean b = patientService.updateById(patient);
        return Result.ok();
    }
    @ApiOperation("删除诊人信息 ")
    //删除就诊人
    @DeleteMapping("auth/remove/{patientId}")
    public Result updatePatient(@PathVariable String patientId) {
        boolean b = patientService.removeById(patientId);
        return Result.ok();
    }
    @ApiOperation(value = "获取就诊人")
    @GetMapping("inner/get/{id}")
    public Patient getPatientOrder(
            @ApiParam(name = "id", value = "就诊人id", required = true)
            @PathVariable("id") Long id) {
        return patientService.getById(id);
    }




}

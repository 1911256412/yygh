package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.result.Result;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import net.bytebuddy.asm.Advice;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/admin/hosp/department")
//@CrossOrigin
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;

    @GetMapping("getDeparment/{hoscode}")
    public Result selectDeparment(@PathVariable  String hoscode){
        List<DepartmentVo> departmentVoList=departmentService.selectDeparment(hoscode);
        return  Result.ok(departmentVoList);
    }

}

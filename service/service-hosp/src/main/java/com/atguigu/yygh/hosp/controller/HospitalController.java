package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.result.Result;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.models.auth.In;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/admin/hosp/hospital")
//@CrossOrigin
@Api(tags = "医院")
public class HospitalController {

    @Resource
    private HospitalService hospitalService;

    @GetMapping("selectPage/{current}/{limit}")
    @ApiOperation("按照条件分页查询医院列表")
    public Result selectPage( @PathVariable Integer current,
                              @PathVariable Integer limit,
                             HospitalQueryVo hospitalQueryVo){
     Page<Hospital> hospitalPage=hospitalService.selectByPage(current,limit,hospitalQueryVo);
        List<Hospital> content = hospitalPage.getContent();
        int totalPages = hospitalPage.getTotalPages();
        return Result.ok(hospitalPage);
    }
    //修改医院上线状态
    @ApiOperation("修改医院上线状态 ")
    @PostMapping("update/{id}/{status}")
    public Result updateStatus(@PathVariable  String id ,@PathVariable  String status){
        hospitalService.updateStatus(id,status);
        return Result.ok();
    }
    @ApiOperation(value = "获取医院详情")
    @GetMapping("show/{id}")
    public Result show(
            @ApiParam(name = "id", value = "医院id", required = true)
            @PathVariable String id) {
        return Result.ok(hospitalService.show(id));
    }



}

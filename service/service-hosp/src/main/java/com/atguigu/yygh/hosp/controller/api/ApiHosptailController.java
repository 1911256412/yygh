package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.result.Result;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp/hospital")
@Api(tags = "首页展示接口 ")
public class ApiHosptailController {

    @Resource
    private ScheduleService scheduleService;
    @Resource
    private HospitalService hospitalService;
    @Resource
    private DepartmentService departmentService;

    @ApiOperation("显示医院列表 ")
    @GetMapping("{current}/{limit}")
    public Result getByCondition(@PathVariable Integer current,
                                 @PathVariable Integer limit ,
                                 HospitalQueryVo hospitalQueryVo){
        Page<Hospital> hospitalPage = hospitalService.selectByPage(current, limit, hospitalQueryVo);
        return Result.ok(hospitalPage);
    }
    @ApiOperation("根据医院名称模糊查询医院列表")
    @GetMapping("findByHosname/{hosname}")
    public Result findByHosname (@PathVariable  String hosname){

        List<Hospital> hospitalList=hospitalService.selectByhosname(hosname);
        return Result.ok(hospitalList);
    }
    @ApiOperation("获取科室列表")
    @GetMapping("department/{hoscode}")
    public Result index(@PathVariable String hoscode){
        List<DepartmentVo> departmentVos = departmentService.selectDeparment(hoscode);

        return Result.ok(departmentVos);
    }

    @ApiOperation(value = "医院预约挂号详情")
    @GetMapping("findHospDetail/{hoscode}")
    public Result item(@PathVariable  String hoscode){
        Map<String ,Object > map=hospitalService.item(hoscode);
        return Result.ok(map);
    }
    //获取可预约的排班数据
    @ApiOperation(value = "获取可预约排班数据")
    @GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getBookingSchedule(
            @PathVariable Integer page,
            @PathVariable Integer limit,
            @PathVariable String hoscode,
            @PathVariable String depcode) {
        return Result.ok(scheduleService.getBookingScheduleRule(page, limit, hoscode, depcode));
    }

    @ApiOperation(value = "获取排班数据")
    @GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public Result findScheduleList(
            @PathVariable String hoscode,
            @PathVariable String depcode,
            @PathVariable String workDate) {

        return Result.ok(scheduleService.getShcedule(hoscode, depcode, workDate));
    }
    //根据排班id查询排班信息
    @ApiOperation(value = "根据排班id获取排班数据")
    @GetMapping("getSchedule/{scheduleId}")
    public Result getSchedule(
            @PathVariable String scheduleId) {
        return Result.ok(scheduleService.getById(scheduleId));
    }
    @ApiOperation(value = "根据排班id获取预约下单数据")
    @GetMapping("inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(
            @PathVariable("scheduleId") String scheduleId) {
        return scheduleService.getScheduleOrderVo(scheduleId);
    }





}

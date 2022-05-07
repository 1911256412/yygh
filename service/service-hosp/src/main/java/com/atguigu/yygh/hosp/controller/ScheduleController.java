package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.result.Result;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/hosp/schedule")
//@CrossOrigin
public class ScheduleController {

    @Resource
    private ScheduleService scheduleService;
    @GetMapping("getSchedule/{current}/{limit}/{hoscode}/{depcode}")
    @ApiOperation("按照条件分页查询排班信息")
    public Result getSchedule(@PathVariable  Integer current ,
                              @PathVariable Integer limit ,
                              @PathVariable String hoscode,
                              @PathVariable  String depcode){

        Map<String,Object> map=scheduleService.selectByCondiction(current,limit,hoscode,depcode);
        return Result.ok(map);
    }
    @ApiOperation("根据条件查询排班详情信息 ")
    @GetMapping("getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public Result getScheduleDeatil (@PathVariable  String hoscode,
                                     @PathVariable String depcode,
                                     @PathVariable String workDate){
    List<Schedule> list=scheduleService.getScheduleDeatil(hoscode,depcode,workDate);
        return Result.ok(list);
    }
}

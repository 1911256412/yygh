package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.handler.YyghException;
import com.atguigu.yygh.helper.HttpRequestHelper;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.hosp.service.impl.ScheduleServiceImpl;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.result.Result;
import com.atguigu.yygh.result.ResultCodeEnum;
import com.atguigu.yygh.utils.MD5;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import com.atguigu.yygh.vo.order.SignInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "医院管理API接口")
@RestController
@RequestMapping("/api/hosp")
@Slf4j
public class ApiController {

    @Autowired
    private HospitalService hospitalService;

    @Resource
    private HospitalSetService hospitalSetService;

    @Resource
    private DepartmentService departmentService;

    @Resource
    private ScheduleService scheduleService;

    //上传医院接口
    @PostMapping("/saveHospital")
    public Result savehospital(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        //1、获取医院传递过来的签名,签名进行md5加密
        String sign = (String) map.get("sign");
        //2、根据传递过来的编码 ，在数据库中查询签名 ，
        String hoscode = (String) map.get("hoscode");
        //3、查询mysql数据 得到签名
        String signkey = hospitalSetService.selectkey(hoscode);
        //4、把数据库中查询出来的签名进行MD5加密
        if (signkey != null) {
            String encrypt = MD5.encrypt(signkey);
            //判断签名是否一致
            if (!encrypt.equals(sign)) {
                throw new YyghException(ResultCodeEnum.SIGN_ERROR);
            }
        }
        //传输过程中“+”转换为了“ ”，所以要转换回来
        String logoData = (String) map.get("logoData");
        //替换
        if (!StringUtils.isEmpty(logoData)) {
            String replacelogoData = logoData.replaceAll(" ", "+");
            map.put("logoData", replacelogoData);
        }
        //如果签名一致 存到mongoDB中
        hospitalService.save(map);
        return Result.ok();
    }

    @ApiOperation(value = "获取医院信息")
    @PostMapping("hospital/show")
    public Result hospital(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        //1、获取医院传递过来的签名,签名进行md5加密
        String sign = (String) map.get("sign");
        //2、根据传递过来的编码 ，在数据库中查询签名 ，
        String hoscode = (String) map.get("hoscode");
        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if (!HttpRequestHelper.isSignEquals(map, hospitalSetService.selectkey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        Hospital hospital = hospitalService.getByHocode(hoscode);

        return Result.ok(hospital);
    }

    @ApiOperation(value = "上传科室")
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        //1、获取医院传递过来的签名,签名进行md5加密
        String sign = (String) map.get("sign");
        //2、根据传递过来的编码 ，在数据库中查询签名 ，
        String hoscode = (String) map.get("hoscode");
        //3、查询mysql数据 得到签名
        String signkey = hospitalSetService.selectkey(hoscode);
        //4、把数据库中查询出来的签名进行MD5加密
        if (signkey != null) {
            String encrypt = MD5.encrypt(signkey);
            //判断签名是否一致
            if (!encrypt.equals(sign)) {
                throw new YyghException(ResultCodeEnum.SIGN_ERROR);
            }
        }
        departmentService.save(map);

        return Result.ok();
    }

    @ApiOperation(value = "获取分页列表")
    @PostMapping("department/list")
    public Result department(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        //1、获取医院传递过来的签名,签名进行md5加密
        String sign = (String) map.get("sign");
        //2、根据传递过来的编码 ，在数据库中查询签名 ，
        String hoscode = (String) map.get("hoscode");
        String depcode = (String) map.get("depcode");
        //签名校验
        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if (!HttpRequestHelper.isSignEquals(map, hospitalSetService.selectkey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        int page = StringUtils.isEmpty((String) map.get("page")) ? 1 : Integer.parseInt((String) map.get("page"));
        int limit = StringUtils.isEmpty((String) map.get("limit")) ? 10 : Integer.parseInt((String) map.get("limit"));
        DepartmentQueryVo departmentVo = new DepartmentQueryVo();
        departmentVo.setDepcode(depcode);
        departmentVo.setHoscode(hoscode);
        Page<Department> departmentPage = departmentService.selectByPage(page, limit, departmentVo);
        return Result.ok(departmentPage);
    }

    @ApiOperation(value = "删除科室")
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        //1、获取医院传递过来的签名,签名进行md5加密
        String sign = (String) map.get("sign");
        //2、根据传递过来的编码 ，在数据库中查询签名 ，
        String hoscode = (String) map.get("hoscode");
        String depcode = (String) map.get("depcode");
        //签名校验
        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if (!HttpRequestHelper.isSignEquals(map, hospitalSetService.selectkey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        departmentService.remove(hoscode, depcode);
        return Result.ok();
    }
    //上传排班接口
    @ApiOperation(value = "上传排班")
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        //1、获取医院传递过来的签名,签名进行md5加密
        String sign = (String) map.get("sign");
        //2、根据传递过来的编码 ，在数据库中查询签名 ，
        String hoscode = (String) map.get("hoscode");
        String depcode = (String) map.get("depcode");
        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if (!HttpRequestHelper.isSignEquals(map, hospitalSetService.selectkey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        scheduleService.saveSchedule(map);
        return Result.ok();
    }
    //显示排班
    @ApiOperation(value = "获取排班分页列表")
    @PostMapping("schedule/list")
    public Result schedule(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        //1、获取医院传递过来的签名,签名进行md5加密
        String sign = (String) map.get("sign");
        //2、根据传递过来的编码 ，在数据库中查询签名 ，
        String hoscode = (String) map.get("hoscode");
        String depcode = (String) map.get("depcode");
        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if (!HttpRequestHelper.isSignEquals(map, hospitalSetService.selectkey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        int page = StringUtils.isEmpty(map.get("page")) ? 1 : Integer.parseInt(map.get("page").toString());
        int limit =StringUtils.isEmpty(map.get("limit")) ? 10 : Integer.parseInt(map.get("limit").toString());
        ScheduleQueryVo scheduleQueryVo=new ScheduleQueryVo();
        scheduleQueryVo.setDepcode(depcode);
        scheduleQueryVo.setHoscode(hoscode);
        Page<Schedule> pageModel=scheduleService.selectBypage(page,limit,scheduleQueryVo);
        return Result.ok(pageModel);
    }
    @ApiOperation(value = "删除科室")
    @PostMapping("schedule/remove")
    public Result removeSchedule(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        //1、获取医院传递过来的签名,签名进行md5加密
        //必填
        String hosScheduleId = (String)map.get("hosScheduleId");

        String sign = (String) map.get("sign");
        //2、根据传递过来的编码 ，在数据库中查询签名 ，
        String hoscode = (String) map.get("hoscode");
        String depcode = (String) map.get("depcode");
        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //签名校验
        if (!HttpRequestHelper.isSignEquals(map, hospitalSetService.selectkey(hoscode))) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        scheduleService.remove(hoscode,hosScheduleId);
        return Result.ok();
    }
    //获取医院签名信息
    @ApiOperation(value = "获取医院签名信息")
    @GetMapping("inner/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(
            @PathVariable("hoscode") String hoscode) {
        return hospitalSetService.getSignInfoVo(hoscode);
    }

}
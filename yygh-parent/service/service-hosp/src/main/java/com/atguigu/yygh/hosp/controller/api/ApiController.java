package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.handler.YyghException;
import com.atguigu.yygh.helper.HttpRequestHelper;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.result.Result;
import com.atguigu.yygh.result.ResultCodeEnum;
import com.atguigu.yygh.utils.MD5;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    //上传医院接口
    @PostMapping("/saveHospital")
    public Result  savehospital(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        //1、获取医院传递过来的签名,签名进行md5加密
        String  sign = (String )map.get("sign");
        //2、根据传递过来的编码 ，在数据库中查询签名 ，
        String hoscode= (String )map.get("hoscode");
        //3、查询mysql数据 得到签名
        String signkey=hospitalSetService.selectkey(hoscode);
        //4、把数据库中查询出来的签名进行MD5加密
        if(signkey!=null){
        String encrypt = MD5.encrypt(signkey);
        //判断签名是否一致
        if(!encrypt.equals(sign)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        }
        //传输过程中“+”转换为了“ ”，所以要转换回来
        String  logoData = (String )map.get("logoData");
        //替换
        if(!StringUtils.isEmpty(logoData)){
            String replacelogoData = logoData.replaceAll(" ", "+");
            map.put("logoData",replacelogoData);
            }
        //如果签名一致 存到mongoDB中
        hospitalService.save(map);
        return Result.ok();
    }
}

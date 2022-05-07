package com.atguigu.yygh.msm.controller;

import com.atguigu.yygh.msm.service.MsmService;
import com.atguigu.yygh.msm.util.RandomUtil;
import com.atguigu.yygh.result.Result;
import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/msm")
@Api(tags = "发送短信验证码")
public class MsmController {

    @Resource
    private MsmService msmService;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("send/{phone}")
    public Result send(@PathVariable String phone) {
        //先判断redis是否有验证码
        String code = redisTemplate.opsForValue().get(phone);
        if (!StringUtils.isEmpty(code)) {
            // 如果不等于空验证码存在
            return Result.ok(code);
        }
        code = RandomUtil.getFourBitRandom();
        System.out.println("验证码"+code);
        //没有验证码，生成验证码发送验证码
        boolean b = msmService.send(phone,code);
        if (b) {
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            return Result.ok(code);

        }
        return Result.fail().message("发送短信验证码失败 ");

    }

}

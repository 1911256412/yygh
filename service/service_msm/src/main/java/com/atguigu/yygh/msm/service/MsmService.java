package com.atguigu.yygh.msm.service;

import com.atguigu.yygh.vo.msm.MsmVo;

public interface MsmService {
    boolean send(String phone, String code);
    //rabbitmq的发送短信方法
    boolean send (MsmVo msmVo);
}

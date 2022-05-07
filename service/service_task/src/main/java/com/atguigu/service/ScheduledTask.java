package com.atguigu.service;

import com.atguigu.rabbitmq.common.constant.MqConst;
import com.atguigu.rabbitmq.service.RabbitService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ScheduledTask {

    @Resource
    private RabbitService rabbitService;


    @Scheduled(cron = "0 36 20 * * ?")
    public void task(){
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,MqConst.ROUTING_TASK_8,"1111");

    }
}

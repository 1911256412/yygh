package com.atguigu.rabbitmq.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RabbitService {

    @Resource
    private RabbitTemplate rabbitTemplate;
    /**
     * @param exchange 交换机
     * @param routeKey 路由键
     * @param message 消息
     * @return
     */
    public boolean sendMessage(String exchange ,String routeKey,Object message){
        System.out.println("消息发送");
        rabbitTemplate.convertAndSend(exchange,routeKey,message);
        System.out.println("消息发送完毕");
        return true;
    }

}

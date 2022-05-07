package com.atguigu.yygh.hosp.revice;

import com.atguigu.rabbitmq.common.constant.MqConst;
import com.atguigu.rabbitmq.service.RabbitService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import com.rabbitmq.client.Channel;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
public class HospRevice {
    @Resource
    private ScheduleService scheduleService;

    @Resource
    private RabbitService rabbitService;

    @RabbitListener(bindings = @QueueBinding(
                    value = @Queue(value =MqConst.QUEUE_ORDER ),
                    exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER),
                    key ={MqConst.ROUTING_ORDER}
                        ))
    public void receiver(OrderMqVo orderMqVo, Message message , Channel channel ) throws IOException {
        Schedule schedule = scheduleService.getById(orderMqVo.getScheduleId());
        schedule.setReservedNumber(orderMqVo.getReservedNumber());
        schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
        scheduleService.update(schedule);
        //下单成功之后发送短信
        MsmVo msmVo = orderMqVo.getMsmVo();
        if(msmVo!=null ) {
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM,msmVo );
        }
    }

}

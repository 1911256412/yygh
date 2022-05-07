package com.atguigu.order.revicer;

import com.atguigu.order.service.OrderService;
import com.atguigu.rabbitmq.common.constant.MqConst;
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
public class OrderRevice {

    @Resource
    private OrderService orderService;

    @RabbitListener(bindings={@QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_8 ,durable = "true"),
            exchange =@Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key={MqConst.ROUTING_TASK_8}
    )
    })
    private void orderTips (Message message, Channel channel )throws IOException {
        System.out.println("orderTips代码执行 **********");
        String s = orderService.patientTips();
        System.out.println(s );
    }
}

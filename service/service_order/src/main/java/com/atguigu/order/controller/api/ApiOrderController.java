package com.atguigu.order.controller.api;

import com.atguigu.order.service.OrderService;
import com.atguigu.yygh.result.Result;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@Api(tags = "订单接口 ")
@RestController
@RequestMapping("/api/order/orderInfo")
public class ApiOrderController {

    @Resource
    private OrderService orderService;
    @ApiOperation(value = "创建订单")
    @PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
    public Result submitOrder(@PathVariable String scheduleId,
                              @PathVariable Long patientId){
        Long id =orderService.saveOrder(scheduleId,patientId);
        return Result.ok();
    }
    @ApiOperation(value = "获取订单统计数据")
    @PostMapping("inner/getCountMap")
    public Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo) {
        return orderService.getCountMap(orderCountQueryVo);
    }



}

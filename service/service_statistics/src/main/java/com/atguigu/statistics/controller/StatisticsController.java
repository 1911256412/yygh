package com.atguigu.statistics.controller;

import com.atguigu.OrderFeiginClient;
import com.atguigu.yygh.result.Result;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@Api(tags = "统计管理接口")
@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {

    @Resource
    private OrderFeiginClient orderFeiginClient;

    @ApiOperation(value = "获取订单统计数据")
    @GetMapping("getCountMap")
    public Result  getCountMap( OrderCountQueryVo orderCountQueryVo){
        Map<String, Object> countMap = orderFeiginClient.getCountMap(orderCountQueryVo);
        return Result.ok(countMap);

    }
}

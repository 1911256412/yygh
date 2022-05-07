package com.atguigu;


import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(value = "service-order")
@Repository
public interface OrderFeiginClient {

    @PostMapping("/api/order/orderInfo/inner/getCountMap")
    public Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo) ;

}

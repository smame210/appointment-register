package com.study.orderclient;

import com.study.vo.order.OrderCountQueryVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@FeignClient("service-order")
@Service
public interface OrderFeignClient {

    /**
     * 获取订单统计数据
     */
    @RequestMapping(value = "/admin/order/orderInfo/inner/getCountMap",method = RequestMethod.POST)
    Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo);
}

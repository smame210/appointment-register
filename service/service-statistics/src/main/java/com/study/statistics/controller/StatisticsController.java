package com.study.statistics.controller;

import com.study.common.result.Result;
import com.study.orderclient.OrderFeignClient;
import com.study.vo.order.OrderCountQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "统计管理接口")
@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    @ApiOperation(value = "获取订单统计数据")
    @PostMapping("getCountMap")
    public Result getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo){
        return Result.ok(orderFeignClient.getCountMap(orderCountQueryVo));
    }
}

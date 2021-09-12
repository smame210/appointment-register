package com.study.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.common.result.Result;
import com.study.enums.OrderStatusEnum;
import com.study.model.order.OrderInfo;
import com.study.order.service.IOrderInfoService;
import com.study.vo.order.OrderCountQueryVo;
import com.study.vo.order.OrderQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "订单接口")
@RestController
@RequestMapping("/admin/order/orderInfo")
public class OrderController {
    @Autowired
    private IOrderInfoService iOrderInfoService;

    @ApiOperation(value = "获取分页列表")
    @PostMapping("{page}/{limit}")
    public Result index(@PathVariable Long page,
                        @PathVariable Long limit,
                        @RequestBody OrderQueryVo orderQueryVo) {
        Page<OrderInfo> pageParam = new Page<>(page, limit);
        IPage<OrderInfo> pageModel = iOrderInfoService.selectPage(pageParam, orderQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "获取订单状态")
    @GetMapping("getStatusList")
    public Result getStatusList() {
        return Result.ok(OrderStatusEnum.getStatusList());
    }

    @ApiOperation(value = "获取订单")
    @GetMapping("show/{id}")
    public Result get(@PathVariable Long id) {
        return Result.ok(iOrderInfoService.show(id));
    }

    @ApiOperation(value = "获取订单统计数据")
    @PostMapping("inner/getCountMap")
    public Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo) {
        return iOrderInfoService.getCountMap(orderCountQueryVo);
    }
}

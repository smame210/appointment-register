package com.study.order.controller.api;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.common.result.Result;
import com.study.common.utils.AuthContextHolder;
import com.study.enums.OrderStatusEnum;
import com.study.model.order.OrderInfo;
import com.study.order.service.IOrderInfoService;
import com.study.vo.order.OrderQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author smame210
 * @since 2021-09-09
 */
@Api(tags = "订单接口")
@RestController
@RequestMapping("/api/order/info")
public class OrderInfoController {

    @Autowired
    private IOrderInfoService iOrderInfoService;

    @ApiOperation(value = "创建订单")
    @RequestMapping(value = "auth/submitOrder/{scheduleId}/{patientId}", method = RequestMethod.POST)
    public Result submitOrder(@PathVariable String scheduleId,
                              @PathVariable Long patientId) {
        Long orderId = iOrderInfoService.saveOrder(scheduleId, patientId);
        return Result.ok(orderId);
    }

    @ApiOperation(value = "取消预约")
    @GetMapping("auth/cancelOrder/{orderId}")
    public Result cancelOrder(@PathVariable("orderId") Long orderId) {
        Boolean result = iOrderInfoService.cancelOrder(orderId);
        return Result.ok(result);
    }

    @ApiOperation(value = "订单列表（条件查询带分页）")
    @PostMapping("auth/{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit,
                       @RequestBody OrderQueryVo orderQueryVo, HttpServletRequest request) {
        //设置当前用户id
        orderQueryVo.setUserId(AuthContextHolder.getUserId(request));
        Page<OrderInfo> pageParam = new Page<>(page, limit);
        IPage<OrderInfo> pageModel = iOrderInfoService.selectPage(pageParam, orderQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "获取订单状态")
    @GetMapping("auth/getStatusList")
    public Result getStatusList() {
        return Result.ok(OrderStatusEnum.getStatusList());
    }

    @ApiOperation(value = "根据id获取订单")
    @GetMapping("auth/getOrder/{id}")
    public Result getOrder(@PathVariable String id) {
        return Result.ok(iOrderInfoService.getOrder(id));
    }

}

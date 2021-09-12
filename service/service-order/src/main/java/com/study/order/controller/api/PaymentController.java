package com.study.order.controller.api;

import com.study.common.result.Result;
import com.study.enums.PaymentTypeEnum;
import com.study.order.service.IPaymentService;
import com.study.order.service.IWeixinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(tags = "支付接口")
@RestController
@RequestMapping("/api/order/pay")
public class PaymentController {

    @Autowired
    private IWeixinService iWeixinService;
    @Autowired
    private IPaymentService iPaymentService;

    @ApiOperation(value = "生成微信支付二维码")
    @GetMapping("auth/weixin/createPayQrc/{orderId}")
    public Result createWeixinPayQrc(@PathVariable("orderId") Long orderId){
        return Result.ok(iWeixinService.createWeixinPayQrc(orderId));
    }

    @ApiOperation(value = "查询支付状态")
    @GetMapping("auth/queryPayStatus/{orderId}")
    public Result queryPayStatus(@PathVariable("orderId") Long orderId){
        Map<String, String> resultMap = iPaymentService.queryPayStatus(orderId, PaymentTypeEnum.WEIXIN.name());

        if (resultMap == null) {
            return Result.fail().message("支付出错");
        }

        if ("SUCCESS".equals(resultMap.get("trade_state"))) {
            //更改订单状态，处理支付结果
            //TODO 如果支付了但更改订单状态失败怎么办？
            String out_trade_no = resultMap.get("out_trade_no");
            iPaymentService.paySuccess(out_trade_no, PaymentTypeEnum.WEIXIN.getStatus(), resultMap);
            return Result.ok().message("支付成功");
        }

        return Result.ok().message("支付中");
    }

}

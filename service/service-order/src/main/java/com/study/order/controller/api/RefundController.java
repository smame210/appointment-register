package com.study.order.controller.api;

import com.study.order.service.IRefundInfoService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "退款接口")
@RestController
@RequestMapping("/api/order/refund")
public class RefundController {

    @Autowired
    private IRefundInfoService iRefundInfoService;



}

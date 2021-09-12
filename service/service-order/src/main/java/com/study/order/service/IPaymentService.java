package com.study.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.model.order.OrderInfo;
import com.study.model.order.PaymentInfo;

import java.util.Map;

public interface IPaymentService extends IService<PaymentInfo> {
    void savePaymentInfo(OrderInfo order, Integer paymentType);

    Map<String, String> queryPayStatus(Long orderId, String paymentType);

    void paySuccess(String out_trade_no, Integer paymentType, Map<String, String> resultMap);

    PaymentInfo getPaymentInfo(Long orderId, Integer paymentType);
}

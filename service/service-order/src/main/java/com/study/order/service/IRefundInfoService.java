package com.study.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.model.order.PaymentInfo;
import com.study.model.order.RefundInfo;

public interface IRefundInfoService extends IService<RefundInfo> {
    boolean refund(Long orderId);

    RefundInfo saveRefundInfo(PaymentInfo paymentInfo);
}

package com.study.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.study.common.exception.BizException;
import com.study.common.result.ResultCodeEnum;
import com.study.enums.PaymentTypeEnum;
import com.study.enums.RefundStatusEnum;
import com.study.model.order.PaymentInfo;
import com.study.model.order.RefundInfo;
import com.study.order.mapper.RefundInfoMapper;
import com.study.order.service.IPaymentService;
import com.study.order.service.IRefundInfoService;
import com.study.order.util.ConstantPropertiesUtils;
import com.study.order.util.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements IRefundInfoService {

    @Autowired
    private IPaymentService iPaymentService;

    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {
        QueryWrapper<RefundInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", paymentInfo.getOrderId());
        queryWrapper.eq("payment_type", paymentInfo.getPaymentType());
        RefundInfo refundInfo = baseMapper.selectOne(queryWrapper);

        if(refundInfo == null){
            // 保存退款记录
            refundInfo = new RefundInfo();
            refundInfo.setCreateTime(new Date());
            refundInfo.setOrderId(paymentInfo.getOrderId());
            refundInfo.setPaymentType(paymentInfo.getPaymentType());
            refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
            refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());
            refundInfo.setSubject(paymentInfo.getSubject());
            refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
            int insert = baseMapper.insert(refundInfo);
            if (insert <= 0){
                throw new BizException("退款出错，请重试！", ResultCodeEnum.FAIL.getCode());
            }
        }
        return refundInfo;
    }

    @Override
    public boolean refund(Long orderId) {
        try {
            PaymentInfo paymentInfo = iPaymentService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());
            RefundInfo refundInfo = this.saveRefundInfo(paymentInfo);
            if(refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus().intValue()) {
                return true;
            }

            Map<String,String> paramMap = new HashMap<>();
            paramMap.put("appid",ConstantPropertiesUtils.APPID);       //公众账号ID
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);   //商户编号
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            paramMap.put("transaction_id",paymentInfo.getTradeNo()); //微信订单号
            paramMap.put("out_trade_no",paymentInfo.getOutTradeNo()); //商户订单编号
            paramMap.put("out_refund_no","tk"+paymentInfo.getOutTradeNo()); //商户退款单号
//            paramMap.put("total_fee",paymentInfo.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
//            paramMap.put("refund_fee",paymentInfo.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee","1");
            paramMap.put("refund_fee","1");
            String paramXml = WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            client.setXmlParam(paramXml);
            client.setHttps(true);
            client.setCert(true);
            client.setCertPassword(ConstantPropertiesUtils.PARTNER);
            client.post();

            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //TODO 已经退款但状态更改出错怎么半？
            if (WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))) {
                refundInfo.setCallbackTime(new Date());
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                this.updateById(refundInfo);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}

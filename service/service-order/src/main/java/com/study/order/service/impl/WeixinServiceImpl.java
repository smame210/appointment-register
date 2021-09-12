package com.study.order.service.impl;

import com.github.wxpay.sdk.WXPayUtil;
import com.study.common.exception.BizException;
import com.study.common.result.ResultCodeEnum;
import com.study.enums.PaymentTypeEnum;
import com.study.model.order.OrderInfo;
import com.study.order.service.IOrderInfoService;
import com.study.order.service.IPaymentService;
import com.study.order.service.IWeixinService;
import com.study.order.util.ConstantPropertiesUtils;
import com.study.order.util.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WeixinServiceImpl implements IWeixinService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IOrderInfoService iOrderInfoService;

    @Autowired
    private IPaymentService iPaymentService;

    //创建微信支付二维码
    @Override
    public Map createWeixinPayQrc(Long orderId) {
        try {
            Map redisMap = (Map)redisTemplate.opsForValue().get(orderId.toString());
            if (redisMap != null){
                return redisMap;
            }
            //根据id获取订单信息
            OrderInfo order = iOrderInfoService.getById(orderId);
            // 保存交易记录
            iPaymentService.savePaymentInfo(order, PaymentTypeEnum.WEIXIN.getStatus());
            //1、设置参数
            Map paramMap = new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            String body = order.getReserveDate() + "就诊"+ order.getDepname();
            paramMap.put("body", body);
            paramMap.put("out_trade_no", order.getOutTradeNo());
            //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee", "1"); //金额0.01
            paramMap.put("spbill_create_ip", "127.0.0.1");
            paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
            paramMap.put("trade_type", "NATIVE");

            //2、HTTPClient来根据URL访问第三方接口并且传递参数
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();

            //3、返回第三方的数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4、封装返回结果集
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", order.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url"));
            if(null != resultMap.get("result_code")) {
                //微信支付二维码2小时过期，可采取2小时未支付取消订单
                redisTemplate.opsForValue().set(orderId.toString(), map, 1000, TimeUnit.MINUTES);
            }
            return map;
        } catch (Exception e) {
            throw new BizException(ResultCodeEnum.GET_PAYMENTQRC_ERROR);
        }

    }
}


package com.study.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.wxpay.sdk.WXPayUtil;
import com.study.common.exception.BizException;
import com.study.common.helper.HttpRequestHelper;
import com.study.common.result.ResultCodeEnum;
import com.study.enums.OrderStatusEnum;
import com.study.enums.PaymentStatusEnum;
import com.study.hospclient.HospFeignClient;
import com.study.model.order.OrderInfo;
import com.study.model.order.PaymentInfo;
import com.study.order.mapper.PaymentInfoMapper;
import com.study.order.service.IOrderInfoService;
import com.study.order.service.IPaymentService;
import com.study.order.util.ConstantPropertiesUtils;
import com.study.order.util.HttpClient;
import com.study.vo.order.SignInfoVo;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements IPaymentService {

    @Autowired
    private IOrderInfoService iOrderInfoService;

    @Autowired
    private HospFeignClient hospFeignClient;

    @Override
    public void savePaymentInfo(OrderInfo orderInfo, Integer paymentType) {
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderInfo.getId());
        wrapper.eq("payment_type", paymentType);
        Integer count = baseMapper.selectCount(wrapper);
        if (count > 0){
            return;
        }

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")+"|"+orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle();
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(orderInfo.getAmount());
        baseMapper.insert(paymentInfo);
    }

    @Override
    public Map<String, String> queryPayStatus(Long orderId, String paymentType) {
        try {
            OrderInfo orderInfo = iOrderInfoService.getById(orderId);
            //1、封装参数
            Map paramMap = new HashMap<>();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //2、设置请求
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
            //3、返回第三方的数据，转成Map
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4、返回
            return resultMap;
        } catch (Exception e) {
            throw new BizException(ResultCodeEnum.SERVER_BUSY);
        }
    }

    @Override
    public void paySuccess(String out_trade_no, Integer paymentType, Map<String, String> resultMap) {
        PaymentInfo paymentInfo = this.getPaymentInfo(out_trade_no, paymentType);
        if (null == paymentInfo) {
            throw new BizException(ResultCodeEnum.DATA_ERROR);
        }
        if (!Objects.equals(paymentInfo.getPaymentStatus(), PaymentStatusEnum.UNPAID.getStatus())) {
            return;
        }

        //修改支付状态
        PaymentInfo paymentInfoUpd = new PaymentInfo();
        paymentInfoUpd.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfoUpd.setTradeNo(resultMap.get("transaction_id"));
        paymentInfoUpd.setCallbackTime(new Date());
        paymentInfoUpd.setCallbackContent(resultMap.toString());
        this.updatePaymentInfo(out_trade_no, paymentInfoUpd);
        //修改订单状态
        OrderInfo orderInfo = iOrderInfoService.getById(paymentInfo.getOrderId());
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        iOrderInfoService.updateById(orderInfo);

        // 调用医院接口，通知更新支付状态
        SignInfoVo signInfoVo = hospFeignClient.getSignInfoVo(orderInfo.getHoscode());
        if(null == signInfoVo) {
            throw new BizException(ResultCodeEnum.DATA_ERROR);
        }
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode",orderInfo.getHoscode());
        reqMap.put("hosRecordId",orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);
        JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl()+"/order/updatePayStatus");
        if(result.getInteger("code") != 200) {
            throw new BizException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }

    }

    /**
     * 获取支付记录
     */
    private PaymentInfo getPaymentInfo(String outTradeNo, Integer paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no", outTradeNo);
        queryWrapper.eq("payment_type", paymentType);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 更改支付记录
     */
    private void updatePaymentInfo(String outTradeNo, PaymentInfo paymentInfo) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no", outTradeNo);
        baseMapper.update(paymentInfo, queryWrapper);
    }

    @Override
    public PaymentInfo getPaymentInfo(Long orderId, Integer paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        queryWrapper.eq("payment_type", paymentType);
        return baseMapper.selectOne(queryWrapper);
    }

}

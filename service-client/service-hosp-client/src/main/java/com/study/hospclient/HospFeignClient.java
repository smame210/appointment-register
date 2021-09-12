package com.study.hospclient;

import com.study.vo.hosp.ScheduleOrderVo;
import com.study.vo.order.SignInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("service-hosp")
@Service
public interface HospFeignClient {
    /**
     * 根据排班id获取预约下单数据
     */
    @RequestMapping(value = "/api/hosp/hospital/inner/getScheduleOrderVo/{scheduleId}", method = RequestMethod.GET)
    ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId);

    /**
     * 获取医院签名信息
     */
    @RequestMapping(value = "/api/hosp/hospital/inner/getSignInfoVo/{hoscode}", method = RequestMethod.GET)
    SignInfoVo getSignInfoVo(@PathVariable("hoscode") String hoscode);
}

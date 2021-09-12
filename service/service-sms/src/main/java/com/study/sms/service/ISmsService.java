package com.study.sms.service;

import com.study.vo.msm.MsmVo;

public interface ISmsService {
    boolean sendCode(String phone);

    boolean sendMessage(MsmVo msmVo);
}

package com.study.sms.controller;

import com.study.common.result.Result;
import com.study.sms.service.ISmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "手机验证码管理")
@RestController
@RequestMapping("api/sms")
public class SmsController {

    @Autowired
    private ISmsService iSmsService;

    @ApiOperation(value = "发送验证码到邮箱")
    @GetMapping("send/{phone}")
    public Result sendCodeToMail(@PathVariable String phone){
        boolean isSend = iSmsService.sendCode(phone);
        if (isSend){
            return Result.ok();
        }else {
            return Result.fail().message("发送验证码失败");
        }
    }

}

package com.study.mail.controller;

import com.study.common.result.Result;
import com.study.mail.service.MailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "验证码管理")
@RestController
@RequestMapping("api/email")
public class MailController {

    @Autowired
    private MailService mailService;

    @ApiOperation(value = "发送验证码到邮箱")
    @GetMapping("send/{email}")
    public Result sendCodeToMail(@PathVariable String email){
        boolean isSend = mailService.sendCode(email);
        if (isSend){
            return Result.ok();
        }else {
            return Result.fail().message("发送验证码失败");
        }
    }

}

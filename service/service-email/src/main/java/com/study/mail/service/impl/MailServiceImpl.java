package com.study.mail.service.impl;

import com.study.common.result.Result;
import com.study.common.utils.RandomUtil;
import com.study.mail.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    //邮件发件人
    @Value("${mail.fromMail.addr}")
    private String from;

    @Override
    public boolean sendCode(String email) {
        String code = redisTemplate.opsForValue().get(email);
        if(!StringUtils.isEmpty(code)) {
            return true;
        }else {
            code = RandomUtil.getSixBitRandom();
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setSubject("验证码");
            mailMessage.setText("欢迎使用尚医通预约挂号系统，验证码为" + code + "，有效期为5分钟。");
            mailMessage.setFrom(from);
            mailMessage.setTo(email);
            mailSender.send(mailMessage);

            redisTemplate.opsForValue().set(email, code,5, TimeUnit.MINUTES);
            return true;
        }
    }
}

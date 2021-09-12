package com.study.sms.service.Impl;

import com.study.common.utils.RandomUtil;
import com.study.sms.sdk.CCPRestSDK;
import com.study.sms.service.ISmsService;
import com.study.vo.msm.MsmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Service
public class SmsServiceImpl implements ISmsService {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Value("${cloopen.sms.accountSid}")
    private String accountSid;

    @Value("${cloopen.sms.accountToken}")
    private String accountToken;

    @Value("${cloopen.sms.appId}")
    private String appId;

    @Override
    public boolean sendCode(String phone) {
        String code = redisTemplate.opsForValue().get(phone);
        if(!StringUtils.isEmpty(code)) {
            return true;
        }else {
            code = RandomUtil.getSixBitRandom();

            HashMap<String, Object> result = null;
            CCPRestSDK restAPI = new CCPRestSDK();
            restAPI.init("app.cloopen.com", "8883");
            restAPI.setAccount(accountSid, accountToken);
            restAPI.setAppId(appId);
            result = restAPI.sendTemplateSMS(phone,"1" ,new String[]{code,"5"});

            if("000000".equals(result.get("statusCode"))) {
                //正常返回输出data包体信息（map）
//                HashMap<String, Object> data = (HashMap<String, Object>) result.get("data");
//                Set<String> keySet = data.keySet();
//                for (String key : keySet) {
//                    Object object = data.get(key);
//                }
                redisTemplate.opsForValue().set(phone, code,5, TimeUnit.MINUTES);
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean sendMessage(MsmVo msmVo) {
        HashMap<String, Object> result = null;
        CCPRestSDK restAPI = new CCPRestSDK();
        restAPI.init("app.cloopen.com", "8883");
        restAPI.setAccount(accountSid, accountToken);
        restAPI.setAppId(appId);
        String code = (String) msmVo.getParam().get("code");
        result = restAPI.sendTemplateSMS(msmVo.getPhone(),"1" ,new String[]{code, "5"});

        if("000000".equals(result.get("statusCode"))) {
            return true;
        }
        return false;
    }
}

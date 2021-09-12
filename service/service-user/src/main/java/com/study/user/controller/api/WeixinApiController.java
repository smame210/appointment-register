package com.study.user.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.study.common.exception.BizException;
import com.study.common.result.Result;
import com.study.common.result.ResultCodeEnum;
import com.study.model.user.UserInfo;
import com.study.user.service.IUserInfoService;
import com.study.user.utils.ConstantPropertiesUtil;
import com.study.user.utils.HttpClientUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "微信扫码登录")
@Controller
@RequestMapping("/api/ucenter/wx")
@Slf4j
public class WeixinApiController {

    @Autowired
    private IUserInfoService iUserInfoService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 获取微信登录参数, 生成二维码
     */
    @GetMapping("getLoginParam")
    @ResponseBody
    public Result genQrConnect(){
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("appid", ConstantPropertiesUtil.WX_OPEN_APP_ID);
            String redirectUri = URLEncoder.encode(ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL, "utf-8");
            map.put("redirectUri", redirectUri);
            map.put("scope", "snsapi_login");
            map.put("state", System.currentTimeMillis()+"");
            return Result.ok(map);
        } catch (Exception e){
            return null;
        }
    }

    /**
     * 微信登录回调
     */
    @RequestMapping("callback")
    public String callback(String code, String state){
        if(StringUtils.isEmpty(code) || StringUtils.isEmpty(state)){
            throw new BizException(ResultCodeEnum.ILLEGAL_CALLBACK_REQUEST_ERROR);
        }

        //获取AccessToken
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                ConstantPropertiesUtil.WX_OPEN_APP_ID,
                ConstantPropertiesUtil.WX_OPEN_APP_SECRET,
                code);
        String result;
        try {
            result = HttpClientUtils.get(accessTokenUrl);
        } catch (Exception e) {
            throw new BizException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }

        JSONObject resultJson = JSONObject.parseObject(result);
        if(resultJson.getString("errcode") != null){
            log.error("获取access_token失败：" + resultJson.getString("errcode") + resultJson.getString("errmsg"));
            throw new BizException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }
        String accessToken = resultJson.getString("access_token");
        String openId = resultJson.getString("openid");

        //使用access_token换取受保护的资源：微信的个人信息
        String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                "?access_token=%s" +
                "&openid=%s";
        String userInfoUrl = String.format(baseUserInfoUrl, accessToken, openId);
        String userInfoResult;
        try {
            userInfoResult = HttpClientUtils.get(userInfoUrl);
        } catch (Exception e) {
            throw new BizException(ResultCodeEnum.FETCH_USERINFO_ERROR);
        }

        JSONObject resultUserInfoJson = JSONObject.parseObject(userInfoResult);
        if(resultUserInfoJson.getString("errcode") != null){
            log.error("获取用户信息失败：" + resultUserInfoJson.getString("errcode") + resultUserInfoJson.getString("errmsg"));
            throw new BizException(ResultCodeEnum.FETCH_USERINFO_ERROR);
        }

        //解析用户信息
        String nickname = resultUserInfoJson.getString("nickname");
        String headimgurl = resultUserInfoJson.getString("headimgurl");
        Map<String, Object> info = iUserInfoService.weixinLogin(openId, nickname);

        try {
            return "redirect:" + ConstantPropertiesUtil.YYGH_BASE_URL +
                    "/weixin/callback?token="+info.get("token")+"&openid="+info.get("openid")+"&name="+URLEncoder.encode((String) info.get("name"), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}

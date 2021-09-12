package com.study.user.controller.api;


import com.study.common.result.Result;
import com.study.common.utils.AuthContextHolder;
import com.study.model.user.UserInfo;
import com.study.user.service.IUserInfoService;
import com.study.vo.user.LoginVo;
import com.study.vo.user.UserAuthVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author smame210
 * @since 2021-09-06
 */
@Api(tags = "用户信息表")
@RestController
@RequestMapping("api/user/info")
public class UserInfoApiController {

    @Autowired
    private IUserInfoService iUserInfoService;

    @ApiOperation(value = "用户登录")
    @RequestMapping(value = "login", method = RequestMethod.POST)
    public Result login(@RequestBody LoginVo loginVo){
        Map<String, Object> info = iUserInfoService.login(loginVo);
        return Result.ok(info);
    }

    @ApiOperation(value = "用户认证")
    @RequestMapping(value = "auth/userAuth", method = RequestMethod.POST)
    public Result userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request) {
        boolean result = iUserInfoService.userAuth(AuthContextHolder.getUserId(request), userAuthVo);
        return result? Result.ok(): Result.fail();
    }

    @ApiOperation(value = "获取用户id信息")
    @RequestMapping(value = "auth/getUserInfo", method = RequestMethod.GET)
    public Result getUserInfo(HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId(request);
        UserInfo userInfo = iUserInfoService.getById(userId);
        return Result.ok(userInfo);
    }

}

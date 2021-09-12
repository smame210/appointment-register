package com.study.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.common.result.Result;
import com.study.model.user.Patient;
import com.study.model.user.UserInfo;
import com.study.user.service.IUserInfoService;
import com.study.vo.user.UserInfoQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/admin/user")
public class UserController {

    @Autowired
    private IUserInfoService iUserInfoService;

    @ApiOperation(value = "条件查询用户带分页")
    @PostMapping("list/{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit,
                       @RequestBody UserInfoQueryVo userInfoQueryVo) {
        Page<UserInfo> pageParam  = new Page<>(page, limit);
        IPage<UserInfo> pageModel = iUserInfoService.selectPage(pageParam , userInfoQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "用户锁定")
    @GetMapping("lock/{userId}/{status}")
    public Result lock(
            @PathVariable("userId") Long userId,
            @PathVariable("status") Integer status) {
        iUserInfoService.lock(userId, status);
        return Result.ok();
    }

    @ApiOperation(value = "用户详情")
    @GetMapping("show/{userId}")
    public Result show(@PathVariable Long userId){
        Map<String, Object> userInfo = iUserInfoService.show(userId);
        return Result.ok(userInfo);
    }

    @ApiOperation(value = "认证审批")
    @GetMapping("approval/{userId}/{authStatus}")
    public Result approval(@PathVariable Long userId,
                           @PathVariable Integer authStatus){
        iUserInfoService.approval(userId,authStatus);
        return Result.ok();
    }

}

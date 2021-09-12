package com.study.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.study.model.user.UserInfo;
import com.study.vo.user.LoginVo;
import com.study.vo.user.UserAuthVo;
import com.study.vo.user.UserInfoQueryVo;

import java.util.Map;

public interface IUserInfoService extends IService<UserInfo> {
    Map<String, Object> login(LoginVo loginVo);

    Map<String, Object> weixinLogin(String openId, String nickname);

    boolean userAuth(Long userId, UserAuthVo userAuthVo);

    IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo);

    void lock(Long userId, Integer status);

    Map<String, Object> show(Long userId);

    void approval(Long userId, Integer authStatus);
}

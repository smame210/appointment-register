package com.study.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.exception.BizException;
import com.study.common.helper.JwtHelper;
import com.study.common.result.ResultCodeEnum;
import com.study.enums.AuthStatusEnum;
import com.study.model.user.Patient;
import com.study.model.user.UserInfo;
import com.study.user.mapper.UserInfoMapper;
import com.study.user.service.IPatientService;
import com.study.user.service.IUserInfoService;
import com.study.vo.user.LoginVo;
import com.study.vo.user.UserAuthVo;
import com.study.vo.user.UserInfoQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IUserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private IPatientService iPatientService;

    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        String openid = loginVo.getOpenid();

        //校验参数
        if(StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            throw new BizException(ResultCodeEnum.DATA_ERROR);
        }
        //验证码校验
        String code2 = redisTemplate.opsForValue().get(phone);
        if(null == code2 || !code2.equals(code)){
            throw new BizException(ResultCodeEnum.CODE_ERROR);
        }
        //微信登陆，绑定手机号码
        UserInfo userInfo = null;
        if(!StringUtils.isEmpty(openid)) {
            userInfo = this.getByOpenid(openid);
            if(null != userInfo) {
                userInfo.setPhone(phone);
                baseMapper.updateById(userInfo);
            } else {
                throw new BizException(ResultCodeEnum.DATA_ERROR);
            }
        }

        //手机号直接登录
        if (null == userInfo) {
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("phone", phone);
            userInfo = baseMapper.selectOne(wrapper);
            //校验是否被禁用
            if(userInfo != null && userInfo.getStatus() == 0) {
                throw new BizException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
            }
            //若为空则创建新用户
            if (null == userInfo){
                UserInfo user = new UserInfo();
                user.setName("");
                user.setPhone(phone);
                user.setCreateTime(new Date());
                user.setUpdateTime(new Date());
                user.setStatus(1);
                baseMapper.insert(user);
                userInfo = user;
            }
        }

        //TODO 记录登录

        //返回页面显示名称
        return getTokenMap(userInfo);
    }

    @Override
    public Map<String, Object> weixinLogin(String openId, String nickname) {
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("openid", openId);
        UserInfo userInfo = baseMapper.selectOne(wrapper);
        //校验是否被禁用
        if(userInfo != null && userInfo.getStatus() == 0) {
            throw new BizException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }

        //若为空则创建新用户
        if (null == userInfo){
            UserInfo user = new UserInfo();
            user.setName("");
            user.setOpenid(openId);
            user.setNickName(nickname);
            user.setStatus(1);
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
            user.setStatus(1);
            baseMapper.insert(user);
            userInfo = user;
        }
        //TODO 记录登录

        //返回页面显示名称
        Map<String, Object> map = getTokenMap(userInfo);
        // 当手机号为空时，放入openid值用于绑定手机号
        if(StringUtils.isEmpty(userInfo.getPhone())){
            map.put("openid", userInfo.getOpenid());
        }else{
            map.put("openid", "");
        }
        return map;
    }

    @Override
    public boolean userAuth(Long userId, UserAuthVo userAuthVo) {
        UserInfo userInfo = baseMapper.selectById(userId);
        if(userInfo == null){
            return false;
        }else{
            userInfo.setName(userAuthVo.getName());
            userInfo.setCertificatesType(userAuthVo.getCertificatesType());
            userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
            userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
            userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
            baseMapper.updateById(userInfo);
            return true;
        }
    }

    //分页查询用户列表
    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo) {
        String name = userInfoQueryVo.getKeyword(); //用户名称
        Integer status = userInfoQueryVo.getStatus();//用户状态
        Integer authStatus = userInfoQueryVo.getAuthStatus(); //认证状态
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin(); //开始时间
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd(); //结束时间

        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(name)){
            wrapper.like("name", name);
        }
        if (!StringUtils.isEmpty(status)){
            wrapper.eq("status", status);
        }
        if(!StringUtils.isEmpty(authStatus)){
            wrapper.eq("auth_status", authStatus);
        }
        if(!StringUtils.isEmpty(createTimeBegin)){
            wrapper.ge("create_time", createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)){
            wrapper.le("create_time", createTimeEnd);
        }
        Page<UserInfo> userInfoPage = baseMapper.selectPage(pageParam, wrapper);
        //认证状态和状态用中文表示
        userInfoPage.getRecords().forEach(this::packageUserInfo);

        return userInfoPage;
    }

    //用户锁定
    @Override
    public void lock(Long userId, Integer status) {
        if(status == 0 || status == 1){
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setStatus(status);
            baseMapper.updateById(userInfo);
        }
    }

    //用户详情
    @Override
    public Map<String, Object> show(Long userId) {
        Map<String, Object> map = new HashMap<>();
        UserInfo userInfo = baseMapper.selectById(userId);
        this.packageUserInfo(userInfo);
        map.put("userInfo", userInfo);
        //根据userid查询就诊人信息
        List<Patient> patients = iPatientService.findAllUserId(userId);
        map.put("patientList", patients);
        return map;
    }

    //用户审批
    @Override
    public void approval(Long userId, Integer authStatus) {
        if(authStatus ==2 || authStatus ==-1) {
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }
    }

    private void packageUserInfo(UserInfo userInfo) {
        userInfo.getParam().put("authStatusString",AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        String status = userInfo.getStatus()==0 ? "锁定" : "正常";
        userInfo.getParam().put("statusString", status);
    }

    /**
     * 封装token map
     *
     */
    private Map<String, Object> getTokenMap(UserInfo userInfo){
        Map<String, Object> map = new HashMap<>();
        String name;
        if(!StringUtils.isEmpty(userInfo.getName())){
            name = userInfo.getName();
        }else if(!StringUtils.isEmpty(userInfo.getNickName())){
            name = userInfo.getNickName();
        }else{
            name = userInfo.getPhone();
        }
        map.put("name", name);
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);
        return map;
    }

    public UserInfo getByOpenid(String openId){
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("openid", openId);
        return baseMapper.selectOne(wrapper);
    }
}

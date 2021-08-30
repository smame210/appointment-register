package com.study.hosp.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.common.exception.BizException;
import com.study.common.result.Result;
import com.study.common.utils.MD5;
import com.study.hosp.service.IHospitalSetService;
import com.study.model.hosp.HospitalSet;
import com.study.vo.hosp.HospitalSetQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

/**
 * <p>
 * 医院信息设置表 前端控制器
 * </p>
 *
 * @author smame210
 * @since 2021-08-27
 */
@Api(tags = "医院设置管理")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
@CrossOrigin
public class HospitalSetController{

    @Autowired
    private IHospitalSetService iHospitalSetService;

    @ApiOperation(value = "获取所有医院设置")
    @RequestMapping(value = "findAll", method = RequestMethod.GET)
    public Result findAllHospSet() {
        //调用service的方法
        List<HospitalSet> list = iHospitalSetService.list();
        return Result.ok(list);
    }

    @ApiOperation(value = "逻辑删除医院设置")
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public Result removeHospSet(@PathVariable Long id) {
        boolean flag = iHospitalSetService.removeById(id);
        if(flag) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "findPageHospSet/{current}/{limit}", method = RequestMethod.POST)
    public Result findPageHospSet(@PathVariable Long current,
                           @PathVariable Long limit,
                           @RequestBody(required = false)HospitalSetQueryVo hospitalSetQueryVo){
        Page<HospitalSet> page = new Page<>(current, limit);
        String hosname = hospitalSetQueryVo.getHosname();
        String hoscode = hospitalSetQueryVo.getHoscode();
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(hosname)){
            wrapper.like("hosname", hosname);
        }
        if (!StringUtils.isEmpty(hoscode)){
            wrapper.eq("hoscode", hoscode);
        }
        Page<HospitalSet> hospitalSetPage = iHospitalSetService.page(page, wrapper);
        return Result.ok(hospitalSetPage);
    }

    @ApiOperation(value = "添加医院设置")
    @RequestMapping(value = "saveHospSet", method = RequestMethod.POST)
    public Result saveHospSet(@RequestBody HospitalSet hospitalSet){
        Random random = new Random();
        hospitalSet.setStatus(1);
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis()+""+random.nextInt(1000)));
        boolean save = iHospitalSetService.save(hospitalSet);
        if (save){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    @ApiOperation(value = "通过id获取医院设置")
    @RequestMapping(value = "getHospSet/{id}", method = RequestMethod.GET)
    public Result getHospSet(@PathVariable Long id){
        HospitalSet hospitalSet = iHospitalSetService.getById(id);
        return Result.ok(hospitalSet);
    }

    @ApiOperation(value = "修改医院设置")
    @RequestMapping(value = "updateHospSet", method = RequestMethod.PUT)
    public Result updateHospSet(@RequestBody HospitalSet hospitalSet){
        boolean flag = iHospitalSetService.updateById(hospitalSet);
        if (flag) {
           return Result.ok();
        } else {
           return Result.fail();
        }

    }

    @ApiOperation(value = "批量删除设置")
    @RequestMapping(value = "batchRemove", method = RequestMethod.DELETE)
    public Result batchRemove(@RequestBody List<Long> idList){
        boolean flag = iHospitalSetService.removeByIds(idList);
        return Result.ok();
    }

    @ApiOperation(value = "医院设置锁定和解锁")
    @RequestMapping(value = "lockHospSet/{id}/{status}", method = RequestMethod.PUT)
    public Result lockHospSet(@PathVariable Long id,
                              @PathVariable Integer status){
        HospitalSet hospitalSet = iHospitalSetService.getById(id);
        hospitalSet.setStatus(status);
        iHospitalSetService.updateById(hospitalSet);
        return Result.ok();
    }

    @ApiOperation(value = "发送签名秘钥")
    @RequestMapping(value = "sendKey/{id}", method = RequestMethod.PUT)
    public Result sendKey(@PathVariable Long id){
        HospitalSet hospitalSet = iHospitalSetService.getById(id);
        String signKey = hospitalSet.getSignKey();
        String hoscode = hospitalSet.getHoscode();
        //TODO 发送短信
        return Result.ok();
    }

}

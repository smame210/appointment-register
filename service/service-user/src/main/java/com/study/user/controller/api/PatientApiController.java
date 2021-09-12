package com.study.user.controller.api;

import com.study.common.result.Result;
import com.study.common.utils.AuthContextHolder;
import com.study.model.user.Patient;
import com.study.user.service.IPatientService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Api(tags = "就诊人管理")
@RestController
@RequestMapping("/api/user/patient")
public class PatientApiController {

    @Autowired
    private IPatientService iPatientService;

    @ApiOperation(value = "获取就诊人列表")
    @RequestMapping(value = "auth/findAll", method = RequestMethod.GET)
    public Result findAll(HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId(request);
        List<Patient> list = iPatientService.findAllUserId(userId);
        return Result.ok(list);
    }

    @ApiOperation(value = "添加就诊人")
    @RequestMapping(value = "auth/save", method = RequestMethod.POST)
    public Result savePatient(@RequestBody Patient patient, HttpServletRequest request) {
        //获取当前登录用户id
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);
        patient.setCreateTime(new Date());
        patient.setUpdateTime(new Date());
        iPatientService.save(patient);
        return Result.ok();
    }

    @ApiOperation(value = "根据id获取就诊人信息")
    @RequestMapping(value = "auth/get/{id}", method = RequestMethod.GET)
    public Result getById(@PathVariable Long id){
        Patient patient = iPatientService.getPatientById(id);
        return Result.ok(patient);
    }

    @ApiOperation(value = "修改就诊人")
    @RequestMapping(value = "auth/update", method = RequestMethod.POST)
    public Result updatePatient(@RequestBody Patient patient){
        patient.setUpdateTime(new Date());
        iPatientService.updateById(patient);
        return Result.ok();
    }

    @ApiOperation(value = "删除就诊人")
    @RequestMapping(value = "auth/remove/{id}", method = RequestMethod.DELETE)
    public Result removePatient(@PathVariable Long id){
        iPatientService.removeById(id);
        return Result.ok();
    }

    @ApiOperation(value = "获取就诊人,内部调用")
    @RequestMapping(value = "inner/get/{id}", method = RequestMethod.GET)
    public Patient getPatientOrder(@PathVariable Long id){
        return iPatientService.getPatientById(id);
    }
}

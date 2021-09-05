package com.study.hosp.controller;

import com.study.common.result.Result;
import com.study.hosp.service.IHospitalService;
import com.study.model.hosp.Hospital;
import com.study.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Api(tags = "医院管理")
@RestController
@RequestMapping("/admin/hosp/hospital")
public class HospitalController {

    @Autowired
    private IHospitalService iHospitalService;

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "findPageHosp/{current}/{limit}", method = RequestMethod.POST)
    public Result findPageHosp(@PathVariable Integer current,
                               @PathVariable Integer limit,
                               @RequestBody(required = false) HospitalQueryVo hospitalQueryVo){

        Page<Hospital> page = iHospitalService.findPage(current, limit, hospitalQueryVo);
        return Result.ok(page);
    }

    @ApiOperation(value = "更新医院状态")
    @RequestMapping(value = "updateStatus/{id}/{status}", method = RequestMethod.PUT)
    public Result updateStatus(@PathVariable String id,
                               @PathVariable Integer status){
        iHospitalService.updateStatus(id, status);
        return Result.ok();
    }

    @ApiOperation(value = "获取医院详情")
    @RequestMapping(value = "show/{id}", method = RequestMethod.GET)
    public Result showById(@PathVariable String id){
        Hospital hospital = iHospitalService.getHospById(id);
        return Result.ok(hospital);
    }
}

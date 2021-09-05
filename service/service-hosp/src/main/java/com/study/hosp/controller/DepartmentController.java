package com.study.hosp.controller;

import com.study.common.result.Result;
import com.study.hosp.service.IDepartmentService;
import com.study.vo.hosp.DepartmentVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "科室管理")
@RestController
@RequestMapping("/admin/hosp/department")
public class DepartmentController {

    @Autowired
    private IDepartmentService iDepartmentService;

    @ApiOperation(value = "查询医院所有科室列表")
    @RequestMapping(value = "getDeptList/{hoscode}", method = RequestMethod.GET)
    public Result getDeptList(@PathVariable String hoscode){
        List<DepartmentVo> list = iDepartmentService.findDeptTree(hoscode);
        return Result.ok(list);
    }
}

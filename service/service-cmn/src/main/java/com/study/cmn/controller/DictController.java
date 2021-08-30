package com.study.cmn.controller;


import com.study.cmn.service.IDictService;
import com.study.common.result.Result;
import com.study.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 组织架构表 前端控制器
 * </p>
 *
 * @author smame210
 * @since 2021-08-30
 */
@Api(value = "数据字典接口")
@RestController
@RequestMapping("/admin/cmn/dict")
@CrossOrigin
public class DictController {

    @Autowired
    private IDictService iDictService;

    @ApiOperation(value = "根据数据id查询子数据列表")
    @RequestMapping(value = "findChildData/{id}", method = RequestMethod.GET)
    public Result findChildData(@PathVariable Long id){
        List<Dict> dicts = iDictService.findChildData(id);
        return Result.ok(dicts);
    }

    @ApiOperation(value = "数据字典导出")
    @RequestMapping(value = "exportData", method = RequestMethod.GET)
    public void exportData(HttpServletResponse response){
        iDictService.exportData(response);
    }

    @ApiOperation(value = "数据字典导入")
    @RequestMapping(value = "importData", method = RequestMethod.POST)
    public Result importDictData(MultipartFile file){
        iDictService.importDictData(file);
        return Result.ok();
    }
}

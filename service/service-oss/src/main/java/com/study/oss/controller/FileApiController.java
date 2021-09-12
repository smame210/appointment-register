package com.study.oss.controller;

import com.study.common.result.Result;
import com.study.oss.service.IFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = "文件管理")
@RestController
@RequestMapping("api/oss/file")
public class FileApiController {

    @Autowired
    private IFileService iFileService;

    @ApiOperation(value = "文件上传")
    @RequestMapping(value = "fileUpload", method = RequestMethod.POST)
    public Result fileUpload(MultipartFile file){
        String name = iFileService.upload(file);
        return Result.ok(name);
    }
}

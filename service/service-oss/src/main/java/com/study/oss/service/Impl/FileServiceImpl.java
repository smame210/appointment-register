package com.study.oss.service.Impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.study.oss.service.IFileService;
import com.study.oss.utils.ConstantOssPropertiesUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FileServiceImpl implements IFileService {
    @Override
    public String upload(MultipartFile file) {

        String endpoint =  ConstantOssPropertiesUtils.ENDPOINT;;
        String accessKeyId = ConstantOssPropertiesUtils.ACCESS_KEY_ID;
        String accessKeySecret = ConstantOssPropertiesUtils.SECRET;
        String bucketName = ConstantOssPropertiesUtils.BUCKET;

        //文件路径为日期，文件名为uuid+原文件名
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String fileName = uuid + file.getOriginalFilename();
        String timeUrl = new DateTime().toString("yyyy/MM/dd");
        fileName = timeUrl+"/"+fileName;

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            InputStream inputStream = file.getInputStream();
            ossClient.putObject(bucketName, fileName, inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if(null != ossClient){
                ossClient.shutdown();
            }
        }
        String url = "https://"+bucketName+"."+endpoint+"/"+fileName;
        return url;
    }
}

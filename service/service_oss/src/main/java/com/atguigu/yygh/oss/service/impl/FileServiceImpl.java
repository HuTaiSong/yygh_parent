package com.atguigu.yygh.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.atguigu.yygh.oss.service.FileService;
import com.atguigu.yygh.oss.util.ConstantOssPropertiesUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * @author nicc
 * @version 1.0
 * @className FileServiceImpl
 * @description TODO
 * @date 2022-09-08 09:42
 */
@Service
public class FileServiceImpl implements FileService {


    @Override
    public String upload(MultipartFile file) {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = ConstantOssPropertiesUtils.END_POINT;
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = ConstantOssPropertiesUtils.ACCESS_KEY_ID;
        String accessKeySecret = ConstantOssPropertiesUtils.ACCESS_KEY_SECRET;
        // 填写Bucket名称，例如examplebucket。
        String bucketName = ConstantOssPropertiesUtils.BUCKET_NAME;
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        String fileName = file.getOriginalFilename();

        //按照当前日期，创建文件夹，上传到创建文件夹里面
        //  2021/02/02/01.jpg
        String dateString = new DateTime().toString("yyyy/MM/dd");

        // 填写本地文件的完整路径，例如D:\\localpath\\examplefile.txt。
        // 如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件。
        String objectName =  dateString + "/"+ UUID.randomUUID().toString().replaceAll("-","") + fileName;

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            InputStream inputStream = file.getInputStream();
            ossClient.putObject(bucketName, objectName, inputStream);
        } catch (Exception oe) {
            return "";
        }finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        return "https://"+ConstantOssPropertiesUtils.BUCKET_NAME+"."+ConstantOssPropertiesUtils.END_POINT+"/" + objectName;
    }
}

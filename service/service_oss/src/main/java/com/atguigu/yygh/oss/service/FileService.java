package com.atguigu.yygh.oss.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author nicc
 * @version 1.0
 * @className FileService
 * @description TODO
 * @date 2022-09-08 09:42
 */
public interface FileService {

    /**
     * 文件上传至阿里云
     */
    String upload(MultipartFile file);

}

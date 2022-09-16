package com.atguigu.yygh.msn.service;

/**
 * @author nicc
 * @version 1.0
 * @className MsmService
 * @description TODO
 * @date 2022-09-07 15:05
 */
public interface MsmService {

    //发送验证码
    boolean send(String phone, String code);

}

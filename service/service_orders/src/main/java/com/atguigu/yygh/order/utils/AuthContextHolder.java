package com.atguigu.yygh.order.utils;

import com.atguigu.yygh.common.utils.JwtHelper;

import javax.servlet.http.HttpServletRequest;

/**
 * @author nicc
 * @version 1.0
 * @className AuthContextHolder
 * @description TODO
 * @date 2022-09-08 14:27
 */
//获取当前用户信息工具类
public class AuthContextHolder {
    //获取当前用户id
    public static Long getUserId(HttpServletRequest request) {
        //从header获取token
        String token = request.getHeader("token");
        //jwt从token获取userid
        Long userId = JwtHelper.getUserId(token);
        return userId;
    }
    //获取当前用户名称
    public static String getUserName(HttpServletRequest request) {
        //从header获取token
        String token = request.getHeader("token");
        //jwt从token获取userid
        String userName = JwtHelper.getUserName(token);
        return userName;
    }
}

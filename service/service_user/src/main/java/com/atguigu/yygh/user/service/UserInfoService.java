package com.atguigu.yygh.user.service;

import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author nicc
 * @version 1.0
 * @className UserInfoService
 * @description TODO
 * @date 2022-09-06 15:02
 */
public interface UserInfoService extends IService<UserInfo> {

    /**
     * 认证审批
     * @param userId
     * @param authStatus 2：通过 -1：不通过
     */
    void approval(Long userId, Integer authStatus);

    /**
     * 详情
     * @param userId
     * @return
     */
    Map<String, Object> show(Long userId);

    //会员登录
    Map<String, Object> login(LoginVo loginVo);

    UserInfo findWeiXinByOpenid(String openid);

    /**
     * 用户认证
     * @param id  用户id
     * @param userAuthVo   四个字段值
     */
    void userAuth(Long id, UserAuthVo userAuthVo);

    //用户列表（条件查询带分页）
    IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo);

    /**
     * 用户锁定
     * @param userId
     * @param status 0：锁定 1：正常
     */
    void lock(Long userId, Integer status);
}

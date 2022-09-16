package com.atguigu.yygh.user.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author nicc
 * @version 1.0
 * @className UserController
 * @description TODO
 * @date 2022-09-08 21:05
 */
@RestController
@RequestMapping("/admin/user")
public class UserController {

    @Autowired
    UserInfoService userInfoService;

    //认证审批
    @GetMapping("approval/{userId}/{authStatus}")
    public R approval(@PathVariable Long userId,@PathVariable Integer authStatus) {
        userInfoService.approval(userId,authStatus);
        return R.ok();
    }

    @GetMapping("/show/{userId}")
    public R show(@PathVariable("userId") Long userId){
        Map<String, Object> map = userInfoService.show(userId);
        return R.ok().data(map);
    }

    @GetMapping("{page}/{limit}")
    public R selectPage(@PathVariable Long page, @PathVariable Long limit , UserInfoQueryVo userInfoQueryVo){
        Page<UserInfo> pageParam = new Page<UserInfo>(page,limit);
        IPage<UserInfo> pageResult = userInfoService.selectPage(pageParam, userInfoQueryVo);
        return R.ok().data("pageModel",pageResult);
    }

    @ApiOperation(value = "锁定")
    @GetMapping("lock/{userId}/{status}")
    public R lock(
            @PathVariable("userId") Long userId,
            @PathVariable("status") Integer status){
        //判空
        if(StringUtils.isEmpty(userId)){
            return R.error().message("Id不能为空");
        }
        if(StringUtils.isEmpty(status)){
            return R.error().message("状态不能为空");
        }

        userInfoService.lock(userId, status);
        return R.ok();
    }

}

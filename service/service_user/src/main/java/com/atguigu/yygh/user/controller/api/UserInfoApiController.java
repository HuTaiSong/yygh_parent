package com.atguigu.yygh.user.controller.api;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.utils.AuthContextHolder;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author nicc
 * @version 1.0
 * @className UserInfoApiController
 * @description TODO
 * @date 2022-09-06 21:12
 */
@RestController
@RequestMapping("/api/user")
public class UserInfoApiController {

    @Autowired
    private UserInfoService userInfoService;

    @PostMapping("userAuth")
    public R userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        userInfoService.userAuth(userId,userAuthVo);
        return R.ok().message("提交成功");
    }

    /**
     * 查询当前用户
     * @param request
     * @return
     */
    @GetMapping("auth/getUserInfo")
    public R getUserInfo(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        UserInfo userInfo = userInfoService.getById(userId);

        Integer authStatus = userInfo.getAuthStatus();  //：未认证 1：认证中 2：认证成功 -1：认证失败

        String authStatusString = "";

        AuthStatusEnum[] values = AuthStatusEnum.values();
        for (AuthStatusEnum value : values) {
            Integer status = value.getStatus();
            if(authStatus.intValue() == status.intValue()){
                authStatusString = value.getName();
                break;
            }
        }
        userInfo.getParam().put("authStatusString",authStatusString);
        return R.ok().data("userInfo",userInfo);
    }

    @PostMapping("/login")
    public R login(@RequestBody LoginVo loginVo) {
        //返回name+token
        Map<String, Object> info = userInfoService.login(loginVo);
        return R.ok().data(info);
    }
}

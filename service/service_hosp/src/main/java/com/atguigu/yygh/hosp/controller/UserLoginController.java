package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.result.R;
import org.springframework.web.bind.annotation.*;

/**
 * @author nicc
 * @version 1.0
 * @className UserLoginController
 * @description TODO
 * @date 2022-08-27 15:01
 */

@RestController
@RequestMapping("/admin/hosp")
//@CrossOrigin  //跨域
public class UserLoginController {

    //登录
    @PostMapping("user/login")
    public R login() {
        return R.ok().code(20000).data("token","admin-token");
    }

    //获取当前用户的信息
    @GetMapping("user/info")
    public R info() {
        return R.ok().code(20000).data("roles","admin")
                .data("introduction","I am a super administrator")
                .data("avatar","https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif")
                .data("name","尚硅谷");
    }

}

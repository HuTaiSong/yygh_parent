package com.atguigu.yygh.msn.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.msn.service.MsmService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nicc
 * @version 1.0
 * @className MsmController
 * @description TODO
 * @date 2022-09-07 15:05
 */
@RestController
@RequestMapping("/api/msm")
public class MsmController {

    @Autowired
    private MsmService msmService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @ApiOperation("发送验证码")
    @GetMapping("/send/{phone}")
    public R send(@PathVariable("phone") String phone){

        //根据phone在redis中查询时候已有验证码
        String code = redisTemplate.opsForValue().get(phone);

        //如果已有验证码，在验证码有效期内不能重新发送
        if(!StringUtils.isEmpty(code)){
            return R.ok();
        }

        //生成验证码
        code = (long) (Math.random() * 1000000) + "";

        //调用验证码发送方法
        boolean isSend = msmService.send(phone, code);

        if(isSend) {
            //发送成功，验证码放到redis，设置有效时间
            redisTemplate.opsForValue().set(phone, code);
            return R.ok();
        } else {
            return R.error().message("发送短信失败");
        }
    }
}

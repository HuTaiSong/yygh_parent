package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author nicc
 * @version 1.0
 * @className HospitalController
 * @description TODO
 * @date 2022-09-03 11:43
 */
@Api(description = "医院接口")
@RestController
@RequestMapping("/admin/hosp/hospital")
//@CrossOrigin
public class HospitalController {

    //注入service
    @Autowired
    private HospitalService hospitalService;


    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{limit}")
    public R index(@PathVariable Integer page, @PathVariable Integer limit, HospitalQueryVo hospitalQueryVo) {
        //调用方法
        return R.ok().data("pages",hospitalService.selectPage(page, limit, hospitalQueryVo));
    }

    @ApiOperation(value = "更新状态")
    @GetMapping("updateStatus/{id}/{status}")
    public R lock(
            @PathVariable("id") String id,
            @PathVariable("status") Integer status){

        hospitalService.updateStatus(id, status);
        return R.ok();
    }

    @ApiOperation(value = "医院详情")
    @GetMapping("show/{id}")
    public R show(
            @PathVariable String id) {
        return R.ok().data("hospital",hospitalService.show(id));
    }
}

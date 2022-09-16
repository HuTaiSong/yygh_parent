package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author nicc
 * @version 1.0
 * @className DepartmentController
 * @description TODO
 * @date 2022-09-05 17:14
 */
@RestController
@RequestMapping("/admin/hosp/department")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    //根据医院编号，查询医院所有科室列表
    @ApiOperation(value = "查询医院所有科室列表")
    @GetMapping("getDeptList/{hoscode}")
    public R getDeptList(@PathVariable("hoscode") String hoscode) {
        List<DepartmentVo> list = departmentService.findDepTree(hoscode);
        return R.ok().data("list",list);
    }
}

package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @author nicc
 * @version 1.0
 * @className HospitalController
 * @description TODO
 * @date 2022-08-24 13:23
 *
 *  医院接口
 *
 */
@Api(description = "医院设置接口")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
//@CrossOrigin //跨域
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;
    @Autowired
    private HospitalService hospitalService;


    //批量删除医院设置（逻辑删除）
    @ApiOperation(value = "批量删除医院设置（逻辑删除）")
    @DeleteMapping("batchRemove")
    public R batchRemove(@RequestBody List<Long> idList) {
        hospitalSetService.removeByIds(idList);
        return R.ok();
    }

    // 医院设置锁定和解锁
    @ApiOperation(value = "医院设置锁定和解锁")
    @PutMapping("lockHospitalSet/{id}/{status}")
    public R lockHospitalSet(@PathVariable("id") Long id,
                             @PathVariable("status") Integer status) {
        //根据id查询医院设置信息
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        //设置状态
        hospitalSet.setStatus(status);
        hospitalSet.setUpdateTime(new Date());//设置自动更新可以不写
        //调用方法
        hospitalSetService.updateById(hospitalSet);
        return R.ok();
    }

    @ApiOperation(value = "无条件分页查询")
    @GetMapping("{page}/{limit}")
    public R pageList(@PathVariable("page") Long page , @PathVariable("limit") Long limit ){

        Page<HospitalSet> pageParam = new Page<>(page, limit);
        hospitalSetService.page(pageParam, null);

        List<HospitalSet> rows = pageParam.getRecords();
        long total = pageParam.getTotal();

        return  R.ok().data("total", total).data("rows", rows);
    }

    //查询所有医院设置
    @ApiOperation(value = "医院设置列表")
    @GetMapping("findAll")
    public R findAll() {
        List<HospitalSet> list = hospitalSetService.list();
        return R.ok().data("list", list);
    }

    @ApiOperation(value = "医院设置删除")
    @DeleteMapping("{id}")
    public R removeById(@ApiParam(name = "id", value = "医院设置编号", required = true)
                             @PathVariable Long id){
        boolean b = hospitalSetService.removeById(id);
        return R.ok();
    }

    @ApiOperation(value = "带条件分页查询")
    @PostMapping("{page}/{limit}")
    public R pageQuery(
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit,
            @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo){

        Page<HospitalSet> pageParam = new Page<>(page, limit);

        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();

        if(hospitalSetQueryVo != null){
            String hosname = hospitalSetQueryVo.getHosname();
            String hoscode = hospitalSetQueryVo.getHoscode();

            if (!StringUtils.isEmpty(hosname)) {
                queryWrapper.like("hosname", hosname);
            }
            if (!StringUtils.isEmpty(hoscode) ) {
                queryWrapper.eq("hoscode", hoscode);
            }
        }

        hospitalSetService.page(pageParam, queryWrapper);

        List<HospitalSet> records = pageParam.getRecords();
        long total = pageParam.getTotal();



        return  R.ok().data("total", total).data("rows", records);
    }


    @ApiOperation(value = "添加医院设置")
    @PostMapping("saveHospSet")
    public R save(@RequestBody HospitalSet hospitalSet){

        //判断医院编号是否重复
        String hoscode = hospitalSet.getHoscode();

        //根据hoscode查询
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("hoscode",hoscode);
        //添加查询条件，解决在添加时医院编号与已被删除医院编号冲突问题
        queryWrapper.last(" OR (is_deleted = 1 AND hoscode = '" + hoscode + "')");

        int count = hospitalSetService.count(queryWrapper);

        if(count>0){
            return R.ok().message("医院编号重复，请重新输入！");// code ==20000
        }

        //设置状态 1 使用 0 不能使用
        hospitalSet.setStatus(1);
        hospitalSetService.save(hospitalSet);

        return R.ok();//message为默认值
    }


    @ApiOperation(value = "根据id查询医院设置")
    @GetMapping("getHospSet/{id}")
    public R getById(@PathVariable("id") Long id){
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return R.ok().data("item", hospitalSet);
    }

    @ApiOperation(value = "根据id修改医院设置")
    @PostMapping("updateHospSet")
    public R updateById(@RequestBody HospitalSet hospitalSet){
        hospitalSetService.updateById(hospitalSet);
        return R.ok();
    }

}

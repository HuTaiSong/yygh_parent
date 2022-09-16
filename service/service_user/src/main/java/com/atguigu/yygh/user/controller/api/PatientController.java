package com.atguigu.yygh.user.controller.api;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.service.PatientService;
import com.atguigu.yygh.user.utils.AuthContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author nicc
 * @version 1.0
 * @className PatientController
 * @description TODO
 * @date 2022-09-08 14:23
 */
@RestController
@RequestMapping("/api/user/patient")
public class PatientController {
    @Autowired
    private PatientService patientService;

    //获取就诊人列表
    @GetMapping("auth/findAll")
    public R findAll(HttpServletRequest request) {
        //获取当前登录用户id
        Long userId = AuthContextHolder.getUserId(request);
        List<Patient> list = patientService.findByUserId(userId);
        return R.ok().data("list",list);
    }
    //添加就诊人
    @PostMapping("auth/save")
    public R savePatient(@RequestBody Patient patient, HttpServletRequest request) {
        //获取当前登录用户id
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);
        patientService.save(patient);
        return R.ok();
    }

    //根据id获取就诊人信息
    @GetMapping("auth/get/{id}")
    public R getPatient(@PathVariable Long id) {
        Patient patient = patientService.getById(id);
        return R.ok().data("patient",patient);
    }
    @GetMapping("inner/get/{id}")
    public Patient getPatientById(@PathVariable("id") Long id) {
        return patientService.getById(id);
    }

    //修改就诊人
    @PostMapping("auth/update")
    public R updatePatient(@RequestBody Patient patient) {
        patientService.updateById(patient);
        return R.ok();
    }

    //删除就诊人
    @DeleteMapping("auth/remove/{id}")
    public R removePatient(@PathVariable Long id) {
        patientService.removeById(id);
        return R.ok();
    }
}

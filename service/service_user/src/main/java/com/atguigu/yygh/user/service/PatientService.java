package com.atguigu.yygh.user.service;

import com.atguigu.yygh.model.user.Patient;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author nicc
 * @version 1.0
 * @className PatientService
 * @description TODO
 * @date 2022-09-08 14:19
 */
public interface PatientService extends IService<Patient> {

    /**
     * 根据用户id查询就诊人列表
     * @param userId
     * @return
     */
    List<Patient> findByUserId(Long userId);

    /**
     * 根据就诊人id查询
     * @param id
     * @return
     */
    Patient getById(Long id);
}

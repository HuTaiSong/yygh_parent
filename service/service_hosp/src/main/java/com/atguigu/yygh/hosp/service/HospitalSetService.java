package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.HospitalSet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.models.auth.In;

import java.util.List;

/**
 * @author nicc
 * @version 1.0
 * @className HospitalService
 * @description TODO
 * @date 2022-08-24 13:05
 */
public interface HospitalSetService {
    List<HospitalSet> list();

    boolean removeById(Long id);

    Page<HospitalSet> page(Page<HospitalSet> pageParam, QueryWrapper<HospitalSet> queryWrapper);

    Integer save(HospitalSet hospitalSet);

    HospitalSet getById(Long id);

    Integer updateById(HospitalSet hospitalSet);

    Integer removeByIds(List<Long> idList);

    //计数
    int count(QueryWrapper<HospitalSet> queryWrapper);

    /**
     * 获取签名key
     * @param hoscode
     * @return
     */
    String getSignKey(String hoscode);
}

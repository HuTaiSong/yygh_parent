package com.atguigu.yygh.hosp.service.impl;

import com.atguigu.yygh.hosp.mapper.HospitalSetMapper;

import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author nicc
 * @version 1.0
 * @className HospitalServiceImpl
 * @description TODO
 * @date 2022-08-24 13:05
 */
@Service
public class HospitalSetServiceImpl implements HospitalSetService {

    @Autowired
    private HospitalSetMapper hospitalSetMapper;

    @Override
    public List<HospitalSet> list() {
        return hospitalSetMapper.selectList(null);
    }

    @Override
    public boolean removeById(Long id) {
        return hospitalSetMapper.deleteById(id) > 0;
    }

    @Override
    public Page<HospitalSet> page(Page<HospitalSet> pageParam, QueryWrapper<HospitalSet> queryWrapper) {
        return hospitalSetMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public Integer save(HospitalSet hospitalSet) {
        return hospitalSetMapper.insert(hospitalSet);
    }

    @Override
    public HospitalSet getById(Long id) {
        return hospitalSetMapper.selectById(id);
    }

    @Override
    public Integer updateById(HospitalSet hospitalSet) {
        return hospitalSetMapper.updateById(hospitalSet);
    }

    @Override
    public Integer removeByIds(List<Long> idList) {
        return hospitalSetMapper.deleteBatchIds(idList);
    }

    @Override
    public int count(QueryWrapper<HospitalSet> queryWrapper) {
        return hospitalSetMapper.selectCount(queryWrapper);
    }

    @Override
    public String getSignKey(String hoscode) {
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("hoscode",hoscode);
        HospitalSet hospitalSet = hospitalSetMapper.selectOne(queryWrapper);
        return hospitalSet.getSignKey();
    }
}

package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author nicc
 * @version 1.0
 * @className HospitalService
 * @description TODO
 * @date 2022-09-02 01:09
 */
public interface HospitalService {

    /**
     * 医院系统 调用 该接口实现 上传医院信息
     * @param paramMap
     */
    void save(Map<String, Object> paramMap);

    /**
     * 查询医院
     * @param hoscode
     * @return
     */
    Hospital getByHoscode(String hoscode);

    /**
     * 分页查询
     * @param page 当前页码
     * @param limit 每页记录数
     * @param hospitalQueryVo 查询条件
     */
    Page<Hospital> selectPage(Integer pageNum, Integer limit, HospitalQueryVo hospitalQueryVo);


    /**
     * 更新状态
     * @param id 需要修改的医院id
     * @param status 当前状态
     */
    void updateStatus(String id, Integer status);

    /**
     * 医院详情
     * @param id
     * @return
     */
    Map<String, Object> show(String id);

    String getHospName(String hoscode);

    List<Hospital> findByHosname(String hosname);

    /**
     * 根据医院编号，获取医院信息（基本信息+预约信息）
     */
    Map<String, Object> item(String hoscode);
}

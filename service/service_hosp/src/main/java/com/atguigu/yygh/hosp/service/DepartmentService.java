package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author nicc
 * @version 1.0
 * @className DepartmentService
 * @description TODO
 * @date 2022-09-02 11:26
 */
public interface DepartmentService {

    /**
     * 根据医院编号 和 科室编号获取科室数据
     */
    Department getDepartment(String hoscode, String depcode);

    //根据医院编号，查询医院所有科室列表
    List<DepartmentVo> findDepTree(String hoscode);

    /**
     * 上传科室信息
     * @param paramMap
     */
    void save(Map<String, Object> paramMap);

    /**
     * 分页查询
     * @param page 当前页码
     * @param limit 每页记录数
     * @param departmentQueryVo 查询条件
     * @return
     */
    Page<Department> selectPage(Integer pageNum, Integer limit, DepartmentQueryVo departmentQueryVo);

    /**
     * 删除科室
     * @param hoscode
     * @param depcode
     */
    void remove(String hoscode, String depcode);

    String getDepName(String hoscode, String depcode);

}
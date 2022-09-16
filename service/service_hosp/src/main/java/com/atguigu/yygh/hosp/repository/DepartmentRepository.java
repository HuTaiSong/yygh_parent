package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author nicc
 * @version 1.0
 * @className DepartmentRepository
 * @description TODO
 * @date 2022-09-02 11:21
 */
@Repository
public interface DepartmentRepository extends MongoRepository<Department,String> {
    //根据 医院编号 + 科室编号 查询科室对象
    Department findDepartmentByHoscodeAndDepcode(String hoscode, String depcode);

    List<Department> findDepartmentsByHoscode(String hoscode);
}

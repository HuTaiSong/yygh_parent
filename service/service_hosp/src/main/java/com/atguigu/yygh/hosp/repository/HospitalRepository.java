package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author nicc
 * @version 1.0
 * @className HospitalRepository
 * @description TODO
 * @date 2022-09-02 01:08
 */
@Repository
public interface HospitalRepository extends MongoRepository<Hospital,String> {

    /**
     * 根据Hoscode从mongodb中查询
     * @param hoscode
     * @return
     */
    Hospital findByHoscode(String hoscode);

    List<Hospital> findHospitalByHosnameLike(String hosname);
}

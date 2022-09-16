package com.atguigu.yygh.user.client;

import com.atguigu.yygh.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author nicc
 * @version 1.0
 * @className PatientFeignClient
 * @description TODO
 * @date 2022-09-13 19:24
 */
@FeignClient(value = "service-user")
public interface PatientFeignClient {

    @GetMapping("/api/user/patient/inner/get/{id}")
    public Patient getPatientById(@PathVariable("id") Long id);

}

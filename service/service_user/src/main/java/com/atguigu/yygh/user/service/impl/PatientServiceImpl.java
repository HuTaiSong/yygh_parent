package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.mapper.PatientMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author nicc
 * @version 1.0
 * @className PatientServiceImpl
 * @description TODO
 * @date 2022-09-08 14:19
 */
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

    @Autowired
    private DictFeignClient dictFeignClient;




    //Patient对象里面其他参数封装
    @Override
    public List<Patient> findByUserId(Long userId) {
        QueryWrapper<Patient> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);

        List<Patient> list = baseMapper.selectList(queryWrapper);

        list.forEach(this::packPatient);

        return list;
    }

    @Override
    public Patient getById(Long id) {
        Patient patient = baseMapper.selectById(id);

        return this.packPatient(patient);
    }

    private Patient packPatient(Patient patient) {
        // 就诊人证件类型名称 + 联系人证件类型名称  + 省份名称 + 市名称 + 区名称 + 完整地址
        String certificatesType = patient.getCertificatesType();
        String contactsCertificatesType = patient.getContactsCertificatesType();


        String provinceCode = patient.getProvinceCode();
        String cityCode = patient.getCityCode();
        String districtCode = patient.getDistrictCode();


        String certificatesTypeString = dictFeignClient.getName(certificatesType);

        String contactsCertificatesTypeString = "";
        if(!StringUtils.isEmpty(contactsCertificatesType)){
            contactsCertificatesTypeString = dictFeignClient.getName(contactsCertificatesType);
        }

        String provinceString = dictFeignClient.getName(provinceCode);
        String cityString = dictFeignClient.getName(cityCode);
        String districtString = dictFeignClient.getName(districtCode);


        String fullAddress = provinceString+cityString + districtString + patient.getAddress();

        patient.getParam().put("certificatesTypeString",certificatesTypeString);
        patient.getParam().put("contactsCertificatesTypeString",contactsCertificatesTypeString);
        patient.getParam().put("provinceString",provinceString);
        patient.getParam().put("cityString",cityString);
        patient.getParam().put("districtString",districtString);
        patient.getParam().put("fullAddress",fullAddress);

        return patient;
    }
}

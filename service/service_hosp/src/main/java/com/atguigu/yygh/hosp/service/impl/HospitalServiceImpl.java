package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nicc
 * @version 1.0
 * @className HospitalServiceImpl
 * @description TODO
 * @date 2022-09-02 01:09
 */
@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;
    //注入远程调用数据字典
    @Autowired
    private DictFeignClient dictFeignClient;

    @Override
    public void save(Map<String, Object> paramMap) {
        String jsonString = JSONObject.toJSONString(paramMap);
        //json转对象
        Hospital hospital = JSONObject.parseObject(jsonString, Hospital.class);

        //2 根据医院编号查询医院信息是否已经添加
        Hospital existHospital = hospitalRepository.findByHoscode(hospital.getHoscode());

        //如果已经添加过，修改
        if(existHospital != null) {
            //设置id
            hospital.setId(existHospital.getId());
            hospital.setCreateTime(existHospital.getCreateTime());
        } else {
            //如果没有添加，进行添加操作
            hospital.setCreateTime(new Date());
        }
        hospital.setUpdateTime(new Date());
        hospital.setStatus(1);

        hospitalRepository.save(hospital);
    }

    @Override
    public Hospital getByHoscode(String hoscode) {
        return hospitalRepository.findByHoscode(hoscode);
    }

    @Override
    public Page<Hospital> selectPage(Integer pageNum, Integer limit, HospitalQueryVo hospitalQueryVo) {
        //mongodb待条件的分页查询
        //排序（按照创建时间进行倒序排序）
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");
        //分页参数（mongodb：0 第一页）
        PageRequest pageRequest = PageRequest.of(pageNum - 1, limit);

        //创建实例
        Hospital hospital = new Hospital();

        //通过BeanUtils重新封
        BeanUtils.copyProperties(hospitalQueryVo, hospital);

        //模糊查询
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase(true);
        //创建匹配器，即如何使用查询条件
        Example<Hospital> example = Example.of(hospital, matcher);
        Page<Hospital> pages = hospitalRepository.findAll(example, pageRequest);

        //封装医院等级数据（遍历每一个Hospital对象，封装医院等级名称+省市区）
        pages.getContent().stream().forEach(item -> {
            this.packHospital(item);
        });

        return pages;
    }

    @Override
    public void updateStatus(String id, Integer status) {
        //根据id查询医院信息
        Hospital hospital = hospitalRepository.findById(id).get();

        //更新状态
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());

        //更新到mongodb
        hospitalRepository.save(hospital);
    }

    @Override
    public Map<String, Object> show(String id) {
        //声明封装返回医院信息的map
        Map<String, Object> result = new HashMap<>();

        //根据id查询医院信息
        Hospital hospital = this.packHospital(hospitalRepository.findById(id).get());

        //医院基本信息（包含医院等级）
        result.put("hospital",hospital);
        //单独处理更直观
        result.put("bookingRule", hospital.getBookingRule());

        //不需要重复返回
        hospital.setBookingRule(null);

        return result;
    }

    @Override
    public String getHospName(String hoscode) {
        Hospital hospital = hospitalRepository.findByHoscode(hoscode);
        if(null != hospital) {
            return hospital.getHosname();
        }
        return "";
    }

    @Override
    public List<Hospital> findByHosname(String hosname) {
        return hospitalRepository.findHospitalByHosnameLike(hosname);
    }

    @Override
    public Map<String, Object> item(String hoscode) {
        Map<String, Object> result = new HashMap<>();

        //医院详情
        Hospital hospital = this.packHospital(this.getByHoscode(hoscode));

        result.put("hospital", hospital);
        //预约规则
        result.put("bookingRule", hospital.getBookingRule());

        return result;
    }


    /**
     * 封装数据
     * @param hospital
     * @return
     */
    private Hospital packHospital(Hospital hospital) {

        // 医院等级名称（通过远程服务调用）

        String hostypeString = dictFeignClient.getName(DictEnum.HOSTYPE.getDictCode(),hospital.getHostype());

        // 省份名称（通过远程服务调用）
        String provinceString = dictFeignClient.getName(hospital.getProvinceCode());
        // 城市名称（通过远程服务调用）
        String cityString = dictFeignClient.getName(hospital.getCityCode());
        // 区名称（通过远程服务调用）
        String districtString = dictFeignClient.getName(hospital.getDistrictCode());

        hospital.getParam().put("hostypeString", hostypeString);
        hospital.getParam().put("fullAddress", provinceString + cityString + districtString + hospital.getAddress());

        return hospital;
    }
}

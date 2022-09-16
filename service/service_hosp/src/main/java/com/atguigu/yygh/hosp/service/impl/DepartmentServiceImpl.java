package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.MyException.YyghException;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author nicc
 * @version 1.0
 * @className DepartmentServiceImpl
 * @description TODO
 * @date 2022-09-02 11:27
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    //实现方法：根据医院编号 和 科室编号获取科室数据
    @Override
    public Department getDepartment(String hoscode, String depcode) {
        return departmentRepository.findDepartmentByHoscodeAndDepcode(hoscode, depcode);
    }

    @Override
    public List<DepartmentVo> findDepTree(String hoscode) {
        List<DepartmentVo> bigDeptList = new ArrayList<>();

        //根据hoscode查询所有小科室信息
        Department department = new Department();
        department.setHoscode(hoscode);
        Example<Department> example = Example.of(department);
        List<Department> list = departmentRepository.findAll(example);

        //针对查询到的department集合按照bigcode进行分组
        Map<String, List<Department>> collect = list.stream().collect(
                Collectors.groupingBy(Department::getBigcode)
        );

        //collect遍历
        for(Map.Entry<String, List<Department>> entry : collect.entrySet()){
            //大科室编号
            String bigcode = entry.getKey();
            //大科室对应的小科室集合
            List<Department> value = entry.getValue();

            //封装大科室
            DepartmentVo departmentVo = new DepartmentVo();
            departmentVo.setDepcode(bigcode);//大科室编号
            departmentVo.setDepname(value.get(0).getBigname());//大科室名称

            //转换小科室集合泛型
            List<DepartmentVo> children = new ArrayList<>();
            for (Department dept : value) {
                DepartmentVo deptVo = new DepartmentVo();
                BeanUtils.copyProperties(dept, deptVo);
//                deptVo.setDepcode(dept.getDepcode());
//                deptVo.setDepname(dept.getDepname());
                children.add(deptVo);
            }

            //把小科室list集合放到大科室children里面
            departmentVo.setChildren(children);

            //放到最终result里面
            bigDeptList.add(departmentVo);
        }

        return bigDeptList;
    }

    @Override
    public void save(Map<String, Object> paramMap) {
        // paramMap变成对象
        String jsonString = JSONObject.toJSONString(paramMap);
        Department department = JSONObject.parseObject(jsonString, Department.class);

        if(department == null) {
            throw new YyghException(20001,"数据错误");
        }

        //查询科室是否存在  医院编号 + 科室编号
        Department existsDepartment = departmentRepository.findDepartmentByHoscodeAndDepcode(department.getHoscode(),department.getDepcode());

        //判断
        if(existsDepartment != null) { //修改
            department.setId(existsDepartment.getId());
            department.setCreateTime(existsDepartment.getCreateTime());
            department.setUpdateTime(new Date());
        } else {
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
        }
        departmentRepository.save(department);
    }

    @Override
    public Page<Department> selectPage(Integer pageNum, Integer limit, DepartmentQueryVo departmentQueryVo) {
        //mongodb待条件的分页查询

        //排序（按照创建时间进行倒序排序）
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");
        //分页参数（mongodb：0 第一页）
        PageRequest pageRequest = PageRequest.of(pageNum - 1, limit);

        //创建实例
        Department department = new Department();

        //通过BeanUtils重新封
        BeanUtils.copyProperties(departmentQueryVo, department);

        //模糊查询
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase(true);
        //创建匹配器，即如何使用查询条件
        Example<Department> example = Example.of(department, matcher);
        Page<Department> pages = departmentRepository.findAll(example, pageRequest);

        return pages;
    }

    @Override
    public void remove(String hoscode, String depcode) {
        Department department = departmentRepository.findDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if(null != department) {
            departmentRepository.deleteById(department.getId());
        }
    }

    @Override
    public String getDepName(String hoscode, String depcode) {
        Department department = departmentRepository.findDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if(null != department) {
            return department.getDepname();
        }
        return "";
    }
}

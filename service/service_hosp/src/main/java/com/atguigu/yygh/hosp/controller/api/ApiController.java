package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.common.exception.MyException.YyghException;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.utils.HttpRequestHelper;
import com.atguigu.yygh.common.utils.MD5;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author nicc
 * @version 1.0
 * @className ApiController
 * @description TODO
 * @date 2022-09-02 01:11
 */
@Api(tags = "医院管理API接口")
@RestController
@RequestMapping("/api/hosp")
public class ApiController {

    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private HospitalSetService hospitalSetService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation(value = "上传医院")
    @PostMapping("saveHospital")
    public Result saveHospital(HttpServletRequest request) {

        //获取医院端的所有参数
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());

        //必须进行参数校验
        String hoscode = (String)paramMap.get("hoscode");
        if(StringUtils.isEmpty(hoscode)) {
            throw new YyghException(20001,"医院编号为空");
        }

        //签名校验
        //1 获取医院系统传递过来的签名（MD5加密的）
        String hospSign = (String)paramMap.get("sign");

        //2 根据传递过来医院编码，查询数据库，查询签名（数据库中是未加密的）
        String signKey = hospitalSetService.getSignKey(hoscode);

        //3 把数据库查询签名进行MD5加密
        String signKeyMd5 = MD5.encrypt(signKey);

        //4 判断签名是否一致
        if(!hospSign.equals(signKeyMd5)) {
            throw new YyghException(20001,"签名校验失败");
        }

        //传输过程中“+”转换为了“ ”，因此我们要转换回来
        String logoData = (String)paramMap.get("logoData");
        logoData = logoData.replaceAll(" ","+");
        paramMap.put("logoData",logoData);

        hospitalService.save(paramMap);
        return Result.ok();
    }

    @ApiOperation(value = "获取医院信息")
    @PostMapping("hospital/show")
    public Result hospital(HttpServletRequest request){

        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());

        String hoscode = (String)paramMap.get("hoscode");

        if(StringUtils.isEmpty(hoscode)){
            throw new YyghException(20001,"医院编号为空");
        }

        //校验签名
        String hospSign = (String)paramMap.get("sign");
        String signKey = hospitalSetService.getSignKey(hoscode);
        String signKeyMd5 = MD5.encrypt(signKey);
        if(!hospSign.equals(signKeyMd5)) {
            throw new YyghException(20001,"签名校验失败");
        }

        Hospital hospital = hospitalService.getByHoscode(hoscode);

        return Result.ok(hospital);
    }

    @ApiOperation(value = "上传科室")
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //必须参数校验 略
        String hoscode = (String)paramMap.get("hoscode");
        if(StringUtils.isEmpty(hoscode)){
            throw new YyghException(20001,"医院编号为空");
        }
        String depcode = (String)paramMap.get("depcode");
        if(StringUtils.isEmpty(depcode)){
            throw new YyghException(20001,"科室编号为空");
        }

        //签名校验
        String hospSign = (String)paramMap.get("sign");
        String signKey = hospitalSetService.getSignKey(hoscode);
        String signKeyMd5 = MD5.encrypt(signKey);
        if(!hospSign.equals(signKeyMd5)) {
            throw new YyghException(20001,"签名校验失败");
        }

        departmentService.save(paramMap);
        return Result.ok();
    }

    @ApiOperation(value = "获取分页列表")
    @PostMapping("department/list")
    public Result department(HttpServletRequest request) {

        Map<String, Object> param = HttpRequestHelper.switchMap(request.getParameterMap());

        String hoscode = (String) param.get("hoscode");
        String limit = (String) param.get("limit");
        String page = (String) param.get("page");

        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);

        Page<Department> departments = departmentService.selectPage(Integer.parseInt(page), Integer.parseInt(limit), departmentQueryVo);

        return Result.ok(departments);
    }

    @ApiOperation(value = "上传排班")
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //必须参数校验 略

        scheduleService.save(paramMap);
        return Result.ok();
    }

    @ApiOperation(value = "获取排班分页列表")
    @PostMapping("schedule/list")
    public Result schedule(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());

        String hoscode = (String)paramMap.get("hoscode");
        String depcode = (String)paramMap.get("depcode");

        int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String)paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 10 : Integer.parseInt((String)paramMap.get("limit"));

        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);

        Page<Schedule> pageModel = scheduleService.selectPage(page , limit, scheduleQueryVo);

        return Result.ok(pageModel);
    }

    @ApiOperation(value = "删除排班")
    @PostMapping("schedule/remove")
    public Result removeSchedule(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());

        String hoscode = (String)paramMap.get("hoscode");
        String hosScheduleId = (String)paramMap.get("hosScheduleId");

        scheduleService.remove(hoscode, hosScheduleId);

        return Result.ok();
    }
}

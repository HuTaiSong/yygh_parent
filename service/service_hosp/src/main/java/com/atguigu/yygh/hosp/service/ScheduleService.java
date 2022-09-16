package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author nicc
 * @version 1.0
 * @className ScheduleService
 * @description TODO
 * @date 2022-09-03 02:33
 */
public interface ScheduleService {

    /*更新排班号源数量
     */
    void update(Schedule schedule);

    /**
     * 根据排班id获取预约下单数据（医院+排班数据）
     * @param scheduleId 排班id
     * @return 根据scheduleId查询封装后的ScheduleOrderVo
     */
    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    /**
     * 根据id查询排班列表
     * @param id
     * @return
     */
    Schedule getById(String id);

    /**
     * 获取排班可预约日期数据
     * @param page
     * @param limit
     * @param hoscode
     * @param depcode
     */
    Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode);

    /**
     * 上传排班信息
     * @param paramMap
     */
    void save(Map<String, Object> paramMap);

    /**
     * 分页查询
     * @param pageNum 当前页码
     * @param limit 每页记录数
     * @param scheduleQueryVo 查询条件
     * @return
     */
    Page<Schedule> selectPage(Integer pageNum, Integer limit, ScheduleQueryVo scheduleQueryVo);

    /**
     * 删除排班
     * @param hoscode
     * @param hosScheduleId
     */
    void remove(String hoscode, String hosScheduleId);

    //根据医院编号 和 科室编号 ，查询排班日期
    Map<String, Object> getRuleSchedule(long page, long limit, String hoscode, String depcode);

    //根据医院编号 、科室编号和工作日期，查询排班详细信息
    List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate);
}

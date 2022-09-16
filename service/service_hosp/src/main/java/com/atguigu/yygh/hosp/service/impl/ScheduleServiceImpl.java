package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.MyException.YyghException;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author nicc
 * @version 1.0
 * @className ScheduleServiceImpl
 * @description TODO
 * @date 2022-09-03 02:33
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private DepartmentService departmentService;

    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        scheduleRepository.save(schedule);
    }

    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();

        //1.查询就诊人信息
        Schedule schedule = scheduleRepository.findById(scheduleId).get();

        //2.查询预约规则
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        BookingRule bookingRule = hospital.getBookingRule();
        Department department = departmentService.getDepartment(schedule.getHoscode(), schedule.getDepcode());

        //3.封装ScheduleOrderVo
        scheduleOrderVo.setHoscode(schedule.getHoscode());//医院编号
        scheduleOrderVo.setHosname(hospital.getHosname());//医院名称
        scheduleOrderVo.setDepcode(department.getDepcode());//科室编号
        scheduleOrderVo.setDepname(department.getDepname());//科室名称
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());//排班编号（医院自己的排班主键）
        scheduleOrderVo.setTitle(schedule.getTitle());//医生职称
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());//安排日期(WorkDate)
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());//剩余预约数
        scheduleOrderVo.setReserveTime(scheduleOrderVo.getReserveTime());//安排时间（0：上午 1：下午）
        scheduleOrderVo.setAmount(schedule.getAmount());//医事服务费

        //退号时间
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plus(bookingRule.getQuitDay()).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //挂号开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //预约周期内，最后一天的停止挂号时间
        DateTime endTime = this.getDateTime(new DateTime().plus(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStopTime(stopTime.toDate());


        return scheduleOrderVo;
    }

    @Override
    public Schedule getById(String id) {
        Schedule schedule = scheduleRepository.findById(id).get();
        return this.packSchedule(schedule);
    }

    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        Map<String, Object> result = new HashMap<>();

        //获取预约规则
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if(null == hospital) {
            throw new YyghException();
        }
        BookingRule bookingRule = hospital.getBookingRule();

        //获取可预约日期分页数据，根据bookingRule.cycle
        //注意：排班数据是存储在mongodb中的，这里返回值封装成IPage是mp中的。
        IPage iPage = this.getListDate(page, limit, bookingRule);

        //当前页可预约日期  bookingRule.cycle是几，就有几个日期对象
        List<Date> dateList = iPage.getRecords();

        //按照日期分组，统计预约数
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(dateList);
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")//分组字段
                        .first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("availableNumber").as("availableNumber")
                        .sum("reservedNumber").as("reservedNumber")
        );
        AggregationResults<BookingScheduleRuleVo> aggregationResults =
                mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);

        List<BookingScheduleRuleVo> scheduleVoList = aggregationResults.getMappedResults();

        // list 转成 map
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(scheduleVoList)) {
            scheduleVoMap = scheduleVoList.stream()
                    .collect(
                            Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo)
                    );
        }

        //每一个日期对应一个BookingScheduleRuleVo
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();

        for(int i=0, len=dateList.size(); i<len; i++) {
            Date date = dateList.get(i);

            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);

            if(null == bookingScheduleRuleVo) { // 说明当天没有排班医生
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //就诊医生人数
                bookingScheduleRuleVo.setDocCount(0);
                bookingScheduleRuleVo.setReservedNumber(-1);
                //科室剩余预约数  -1表示无号
                bookingScheduleRuleVo.setAvailableNumber(-1);
                bookingScheduleRuleVo.setWorkDate(date);
            }

            bookingScheduleRuleVo.setWorkDateMd(date);
            //计算当前预约日期为周几
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

            //最后一页最后一条记录为即将预约。
            //0：正常     1：即将放号    -1：当天已停止挂号
            if(i == len-1 && page == iPage.getPages()) {
                bookingScheduleRuleVo.setStatus(1);
            } else {
                bookingScheduleRuleVo.setStatus(0);
            }

            //第一页第一条
            //当天预约如果过了停号时间，状态为停止预约
            if(i == 0 && page == 1) {
                DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if(stopTime.isBeforeNow()) {
                    //停止预约
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }



        // -----------------封装返回结果--------------------------
        Map<String, String> baseMap = new HashMap<>();

        baseMap.put("hosname", hospitalService.getHospName(hoscode));
        baseMap.put("bigname", departmentService.getDepartment(hoscode, depcode).getBigname());
        baseMap.put("depname", departmentService.getDepartment(hoscode, depcode).getDepname());
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        baseMap.put("stopTime", bookingRule.getStopTime());

        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", iPage.getTotal());
        result.put("baseMap", baseMap);

        return result;
    }

    /**
     * 获取可预约日期分页数据
     */
    private IPage<Date> getListDate(int page, int limit, BookingRule bookingRule) {
        //当天放号时间
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        //预约周期
        int cycle = bookingRule.getCycle();
        //如果当天放号时间已过，则预约周期后一天为即将放号时间，周期加1
        if(releaseTime.isBeforeNow())
            cycle += 1;

        //可预约所有日期，最后一天显示即将放号倒计时
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            //计算当前预约日期
            DateTime curDateTime = new DateTime().plusDays(i);
            String dateString = curDateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(dateString).toDate());
        }

        //日期分页，由于预约周期不一样，页面一排最多显示7天数据，多了就要分页显示
        List<Date> pageDateList = new ArrayList<>();

        int start = (page-1)*limit;
        int end = (page-1)*limit+limit;

        if(end >dateList.size())
            end = dateList.size();

        for (int i = start; i < end; i++) {
            pageDateList.add(dateList.get(i));
        }

        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page(page, limit, dateList.size());
        iPage.setRecords(pageDateList);

        return iPage;
    }

    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " "+ timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }

    @Override
    public void save(Map<String, Object> paramMap) {
        //paramMap 转换department对象
        String paramMapString = JSONObject.toJSONString(paramMap);
        Schedule schedule = JSONObject.parseObject(paramMapString, Schedule.class);

        //根据医院编号 和 排班编号查询
        Schedule scheduleExist = scheduleRepository.findScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(),schedule.getHosScheduleId());
        //判断
        if(scheduleExist == null){
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setStatus(1);
        }else{
            schedule.setUpdateTime(new Date());
            schedule.setCreateTime(scheduleExist.getCreateTime());
            schedule.setStatus(1);
        }
        scheduleRepository.save(schedule);
    }

    @Override
    public Page<Schedule> selectPage(Integer pageNum, Integer limit, ScheduleQueryVo scheduleQueryVo) {
        //mongodb待条件的分页查询

        //排序（按照创建时间进行倒序排序）
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");
        //分页参数（mongodb：0 第一页）
        PageRequest pageRequest = PageRequest.of(pageNum - 1, limit);

        //创建实例
        Schedule schedule = new Schedule();

        //通过BeanUtils重新封
        BeanUtils.copyProperties(scheduleQueryVo, schedule);

        //模糊查询
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase(true);
        //创建匹配器，即如何使用查询条件
        Example<Schedule> example = Example.of(schedule, matcher);
        Page<Schedule> pages = scheduleRepository.findAll(example, pageRequest);

        return pages;
    }

    @Override
    public void remove(String hoscode, String hosScheduleId) {
        Schedule schedule = scheduleRepository.findScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if(null != schedule) {
            scheduleRepository.deleteById(schedule.getId());
        }
    }

    @Override
    public Map<String, Object> getRuleSchedule(long page, long limit, String hoscode, String depcode) {
        //1 最终的返回值 （ total  + bookingScheduleRuleVoList + baseMap(hosname)  ）
        Map<String, Object> result = new HashMap<>();

        //2 根据医院编号 和 科室编号 查询
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);

        //3 根据工作日workDate期进行分组(mongodb中的分组查询)
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
                        .first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                Aggregation.sort(Sort.Direction.ASC, "workDate"),

                Aggregation.skip((page - 1) * limit),
                Aggregation.limit(limit)
        );

        //Schedule.class: 查询排班列表
        //BookingScheduleRuleVo.class: 用来封装每一组产出的数据
        //4 调用方法，最终执行
        AggregationResults<BookingScheduleRuleVo> aggResults = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);

        //5 分组结果
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggResults.getMappedResults();

        //6 获取日期对应的星期
        for(BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }



        //----------------------------bookingScheduleRuleVoList 查询完毕 （每一个ruleVo对象中5个属性有值） -------------------------------------

        //7 分组查询的总记录数
        Aggregation totalAggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );

        AggregationResults<BookingScheduleRuleVo> totalAggResults = mongoTemplate.aggregate(totalAggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> mappedResults = totalAggResults.getMappedResults();
        int total = totalAggResults.getMappedResults().size();

        result.put("total",total);
        result.put("bookingScheduleRuleList",bookingScheduleRuleVoList);

        //8 获取医院名称
        //医院名称
        Map<String,Object> baseMap = new HashMap<>();
        baseMap.put("hosname",hospitalService.getHospName(hoscode));

        result.put("baseMap",baseMap);

        //----------------------日期总数---------------------------------------------------------------------------

        return result;
    }

    @Override
    public List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate) {

        List<Schedule> list = scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, new DateTime(workDate).toDate());

        // schedule -->param( hosname + depname + dayOfWeek )
        list.forEach(schedule -> {
            this.packSchedule(schedule);
        });

        return list;
    }

    private Schedule packSchedule(Schedule schedule) {
        // schedule -->param( hosname + depname + dayOfWeek )

        String hospName = hospitalService.getHospName(schedule.getHoscode());
        String depName = departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode());

        Date workDate = schedule.getWorkDate();
        String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));

        schedule.getParam().put("hosname",hospName);
        schedule.getParam().put("depname",depName);
        schedule.getParam().put("dayOfWeek",dayOfWeek);

        return schedule;

    }

    /**
     * 根据日期获取周几数据
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }
}

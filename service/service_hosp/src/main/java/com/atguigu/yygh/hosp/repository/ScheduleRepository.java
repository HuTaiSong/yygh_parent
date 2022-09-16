package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author nicc
 * @version 1.0
 * @className ScheduleRepository
 * @description TODO
 * @date 2022-09-03 02:32
 */
@Repository
public interface ScheduleRepository extends MongoRepository<Schedule,String> {
    /**
     * 根据 医院编号+排班编号 查询排班信息
     * @param hoscode
     * @param hosScheduleId
     * @return
     */
    Schedule findScheduleByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);

    List<Schedule> findScheduleByHoscodeAndDepcodeAndWorkDate(String hoscode, String depcode, Date toDate);
}

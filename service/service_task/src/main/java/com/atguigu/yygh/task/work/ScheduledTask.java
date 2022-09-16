package com.atguigu.yygh.task.work;

import com.atguigu.rabbit.consts.MqConst;
import com.atguigu.rabbit.service.RabbitService;
import com.atguigu.yygh.vo.msm.MsmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author nicc
 * @version 1.0
 * @className ScheduledTask
 * @description TODO
 * @date 2022-09-16 08:50
 */
@Component
@EnableScheduling//开启定时任务
public class ScheduledTask {

    @Autowired
    private RabbitService rabbitService;

    /**
     * 每天8点执行 提醒就诊
     */
    //@Scheduled(cron = "0 0 8 * * ?")
    @Scheduled(cron = "0/5 * * * * ?")//每5s一次
    public void task1() {
        System.out.println(new Date().toLocaleString());
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_8, new MsmVo());
    }
}

package com.atguigu.yygh.hosp.listener;

import com.atguigu.rabbit.consts.MqConst;
import com.atguigu.rabbit.service.RabbitService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author nicc
 * @version 1.0
 * @className HospitalReceiver
 * @description TODO
 * @date 2022-09-13 22:20
 */
@Component
public class HospitalReceiver {

    @Autowired
    ScheduleService scheduleService;

    @Autowired
    RabbitService rabbitService;


    //方法-----监听队列

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER, durable = "true"), //监听哪一个队列
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER), //  队列使用的交换机
            key = {MqConst.ROUTING_ORDER}  // 队列和交换机 进行绑定，使用的key
    ))
    public void receiver(OrderMqVo orderMqVo) throws IOException {
        //if---创建订单
        if(orderMqVo.getAvailableNumber()!=null){
            //参数就是你所接受到的消息
            String scheduleId = orderMqVo.getScheduleId();//mongdob中的排班的id
            Integer reservedNumber = orderMqVo.getReservedNumber();
            Integer availableNumber = orderMqVo.getAvailableNumber();

            Schedule schedule = scheduleService.getById(scheduleId);//mongodb
            schedule.setReservedNumber(reservedNumber);
            schedule.setAvailableNumber(availableNumber);

            scheduleService.update(schedule);

            //医院服务向下一个队列中发送消息
            MsmVo msmVo = orderMqVo.getMsmVo();
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM,MqConst.ROUTING_MSM_ITEM,msmVo);
        }else{
            //取消订单
            String scheduleId = orderMqVo.getScheduleId();
            Schedule schedule = scheduleService.getById(scheduleId);
            schedule.setAvailableNumber(schedule.getAvailableNumber()+1);
            scheduleService.update(schedule);

            MsmVo msmVo = orderMqVo.getMsmVo();
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM,MqConst.ROUTING_MSM_ITEM,msmVo);
        }
    }
}

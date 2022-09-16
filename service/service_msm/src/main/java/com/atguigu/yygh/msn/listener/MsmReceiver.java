package com.atguigu.yygh.msn.listener;

import com.atguigu.rabbit.consts.MqConst;
import com.atguigu.yygh.vo.msm.MsmVo;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author nicc
 * @version 1.0
 * @className MsmReceiver
 * @description TODO
 * @date 2022-09-13 21:16
 */
@Component
public class MsmReceiver {

    //方法-----监听队列
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_MSM_ITEM, durable = "true"), //监听哪一个队列
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_MSM), //  队列使用的交换机
            key = {MqConst.ROUTING_MSM_ITEM}  // 队列和交换机 进行绑定，使用的key
    ))
    public void receiver(MsmVo msmVo) throws IOException {
        //模拟给就诊人发送短信通知
        System.out.println(msmVo.getPhone() + msmVo.getParam().get("message"));
    }

}

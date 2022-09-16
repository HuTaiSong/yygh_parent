package com.atguigu.rabbit.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author nicc
 * @version 1.0
 * @className RabbitService
 * @description TODO
 * @date 2022-09-13 22:09
 */
@Service
public class RabbitService {
    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * @param exchange 交换机  direct
     * @param routingKey  发消息时指定的key
     * @param message  消息
     * @return
     */
    public boolean sendMessage(String exchange, String routingKey, Object message){
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
        return true;
    }
}

package com.atguigu.yygh.order.service.impl;

import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.order.mapper.PaymentMapper;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.order.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * @author nicc
 * @version 1.0
 * @className PaymentServiceImpl
 * @description TODO
 * @date 2022-09-14 09:50
 */
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService {

    @Autowired
    private OrderService orderService;

    @Override
    public PaymentInfo getPaymentInfo(Long orderId, Integer paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",orderId);
        queryWrapper.eq("payment_type",paymentType);

        PaymentInfo paymentInfo = baseMapper.selectOne(queryWrapper);

        return paymentInfo;
    }

    @Override
    public void paySuccess(String outTradeNo, Map<String, String> paramMap) {
        //支付成功后，要做的工作
        //1、修改平台订单的状态
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no",outTradeNo);

        OrderInfo orderInfo = orderService.getOne(queryWrapper);

        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderInfo.setUpdateTime(new Date());
        orderService.updateById(orderInfo);

        //2、修改平台的支付记录的状态
        QueryWrapper<PaymentInfo> paymentInfoQueryWrapper = new QueryWrapper<>();
        paymentInfoQueryWrapper.eq("out_trade_no",outTradeNo);

        PaymentInfo paymentInfo = baseMapper.selectOne(paymentInfoQueryWrapper);

        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfo.setTradeNo(paramMap.get("transaction_id"));//微信端返回的流水号
        paymentInfo.setCallbackTime(new Date());//回调时间=支付成功时间=修改时间
        paymentInfo.setCallbackContent(paramMap.toString());//map==>string
        paymentInfo.setUpdateTime(new Date());

        baseMapper.updateById(paymentInfo);
    }

    @Override
    public void savePaymentInfo(OrderInfo orderInfo, Integer paymentType) {

        //1.检查当前订单是否存在支付记录
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",orderInfo.getId());
        queryWrapper.eq("payment_type", paymentType);

        PaymentInfo paymentInfo = baseMapper.selectOne(queryWrapper);

        if(paymentInfo != null){
            return;
        }

        //2.若当前订单不存在支付记录则创建一条记录
        paymentInfo = new PaymentInfo();

        //赋值
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(paymentType);
//        paymentInfo.setTradeNo(); 交易流水号（支付成功后，微信端给返回的）
//        paymentInfo.setCallbackTime();
//        paymentInfo.setCallbackContent();
        paymentInfo.setTotalAmount(orderInfo.getAmount());
        String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")+"|"+orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle();
        paymentInfo.setSubject(subject);//支付标题
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setUpdateTime(new Date());

        baseMapper.insert(paymentInfo);
    }
}

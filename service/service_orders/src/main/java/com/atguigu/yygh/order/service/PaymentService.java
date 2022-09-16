package com.atguigu.yygh.order.service;

import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author nicc
 * @version 1.0
 * @className PaymentService
 * @description TODO
 * @date 2022-09-14 09:49
 */
public interface PaymentService extends IService<PaymentInfo> {

    /**
     * 查询订单对应的支付记录
     * @param orderId
     * @param paymentType
     * @return
     */
    PaymentInfo getPaymentInfo(Long orderId, Integer paymentType);

    /**
     * 支付成功后，要调用该方法
     * @param outTradeNo  orderInfo.outTradeNo
     * @param paramMap   WeixinService.queryPayStatus() 的返回值
     */
    void paySuccess(String outTradeNo , Map<String, String> paramMap);

    /**
     * 保存交易记录
     * @param orderInfo 订单
     * @param paymentType 支付类型（1：微信 2：支付宝）
     */
    void savePaymentInfo(OrderInfo orderInfo, Integer paymentType);

}

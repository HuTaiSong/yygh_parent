package com.atguigu.yygh.order.service;

import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.order.RefundInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author nicc
 * @version 1.0
 * @className RefundService
 * @description TODO
 * @date 2022-09-15 14:14
 */
public interface RefundService extends IService<RefundInfo> {

    /**
     * 根据支付记录创建退款记录
     * @param paymentInfo
     * @return
     */
    RefundInfo saveRefundInfo(PaymentInfo paymentInfo);

}

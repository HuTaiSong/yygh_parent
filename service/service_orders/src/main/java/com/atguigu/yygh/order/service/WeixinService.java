package com.atguigu.yygh.order.service;

import java.util.Map;

/**
 * @author nicc
 * @version 1.0
 * @className WeixinService
 * @description TODO
 * @date 2022-09-14 10:09
 */
public interface WeixinService {

    /**
     *  创建退款记录  +  微信退款接口 +  修改退款记录的状态以及callback字段值
     * @param orderId
     * @return
     */
    Boolean refund(Long orderId);

    /**  (统一下单)
     * 为指定订单创建二维码支付链接
     * @param orderId
     * @return
     */
    Map createNative(Long orderId);

    /** （查询订单）
     * 查询订单支付状态
     * @param orderId
     * @return
     */
    Map queryPayStatus(Long orderId);

}

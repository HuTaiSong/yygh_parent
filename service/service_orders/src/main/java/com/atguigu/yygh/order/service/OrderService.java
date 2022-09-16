package com.atguigu.yygh.order.service;

import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author nicc
 * @version 1.0
 * @className OrderService
 * @description TODO
 * @date 2022-09-13 19:10
 */
public interface OrderService extends IService<OrderInfo> {

    /**
     * 订单统计
     */
    Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo);

    /**
     * 就诊提醒
     */
    void patientTips();

    /**
     * 取消订单
     * @param orderId
     */
    Boolean cancelOrder(Long orderId);

    /**
     * 查询订单列表
     * @param pageParam 分页参数
     * @param orderQueryVo 分页条件
     * @return 根据条件查询到的当前页
     */
    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo);

    /**
     * 订单详情
     * @param id
     * @return
     */
    OrderInfo getOrderInfo(Long id);

    /**
     * 创建平台端订单
     * @param scheduleId => 平台展示的排班id
     * @param patientId => 就诊人id
     * @return => 生成的平台端订单id
     */
    Long saveOrder(String scheduleId, Long patientId);
}

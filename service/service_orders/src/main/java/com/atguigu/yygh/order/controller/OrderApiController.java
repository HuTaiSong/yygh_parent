package com.atguigu.yygh.order.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.order.utils.AuthContextHolder;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author nicc
 * @version 1.0
 * @className OrderController
 * @description TODO
 * @date 2022-09-13 19:12
 */
@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderApiController {

    @Autowired
    private OrderService orderService;

    @ApiOperation(value = "获取订单统计数据")
    @PostMapping("inner/getCountMap")
    public Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo) {
        return orderService.getCountMap(orderCountQueryVo);
    }

    /**
     * 点击页面上的取消预约按钮
     * @param orderId
     * @return
     */
    @GetMapping("auth/cancelOrder/{orderId}")
    public R cancelOrder(@PathVariable("orderId") Long orderId) {
        Boolean flag = orderService.cancelOrder(orderId);
        return R.ok().data("flag",flag);
    }

    @GetMapping("auth/{page}/{limit}")
    public R list(@PathVariable Long page, @PathVariable Long limit, OrderQueryVo orderQueryVo, HttpServletRequest request) {

        //设置当前用户id
        orderQueryVo.setUserId(AuthContextHolder.getUserId(request));

        if(orderQueryVo == null){
            orderQueryVo = new OrderQueryVo();
        }

        Page<OrderInfo> pageParam = new Page<>(page,limit);
        IPage<OrderInfo> pageModel = orderService.selectPage(pageParam,orderQueryVo);

        return R.ok().data("pageModel",pageModel);
    }

    @GetMapping("auth/getStatusList")
    public R getStatusList() {
        return R.ok().data("statusList", OrderStatusEnum.getStatusList());
    }

    @GetMapping("auth/getOrders/{orderId}")
    public R getOrders(@PathVariable Long orderId) {
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        return R.ok().data("orderInfo",orderInfo);
    }

    @PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
    public R submitOrder(@PathVariable String scheduleId, @PathVariable Long patientId) {
        Long orderId = orderService.saveOrder(scheduleId, patientId);
        return R.ok().data("orderId",orderId);
    }
}

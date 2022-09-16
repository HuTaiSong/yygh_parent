package com.atguigu.yygh.statistics.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.order.client.OrderFeignClient;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author nicc
 * @version 1.0
 * @className StatisticsController
 * @description TODO
 * @date 2022-09-16 21:15
 */
@Api(tags = "统计管理接口")
@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    @GetMapping("getCountMap")
    public R getCountMap(OrderCountQueryVo orderCountQueryVo) {
        Map<String, Object> map = orderFeignClient.getCountMap(orderCountQueryVo);
        return R.ok().data(map);
    }
}

package com.atguigu.yygh.order.mapper;

import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderCountVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author nicc
 * @version 1.0
 * @className OrderInfoMapper
 * @description TODO
 * @date 2022-09-13 19:09
 */
@Repository
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    //统计每天平台预约数据
    List<OrderCountVo> selectOrderCount(OrderCountQueryVo orderCountQueryVo);
}

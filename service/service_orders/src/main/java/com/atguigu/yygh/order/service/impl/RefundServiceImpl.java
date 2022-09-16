package com.atguigu.yygh.order.service.impl;

import com.atguigu.yygh.enums.RefundStatusEnum;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.order.RefundInfo;
import com.atguigu.yygh.order.mapper.RefundInfoMapper;
import com.atguigu.yygh.order.service.RefundService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author nicc
 * @version 1.0
 * @className RefundServiceImpl
 * @description TODO
 * @date 2022-09-15 14:14
 */
@Service
public class RefundServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundService {

    @Autowired
    private RefundInfoMapper refundMapper;

    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {
        //1、判断退款记录是否存在
        QueryWrapper<RefundInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",paymentInfo.getOrderId());

        RefundInfo refundInfo = baseMapper.selectOne(queryWrapper);

        if(refundInfo!=null){
            return refundInfo;
        }

        //2、不存在则创建
        refundInfo = new RefundInfo();

        refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        refundInfo.setOrderId(paymentInfo.getOrderId());
        refundInfo.setPaymentType(paymentInfo.getPaymentType());
        refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
        refundInfo.setSubject(paymentInfo.getSubject());
        refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());//1 -- 退款中（未退款）
//        BeanUtils.copyProperties(paymentInfo,refundInfo);

//        refundInfo.setTradeNo();
//        refundInfo.setCallbackContent();
//        refundInfo.setCallbackTime();

        refundInfo.setCreateTime(new Date());
        refundInfo.setUpdateTime(new Date());


        baseMapper.insert(refundInfo);

        return refundInfo;
    }
}

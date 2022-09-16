package com.atguigu.yygh.order.service.impl;

import com.atguigu.yygh.common.exception.MyException.YyghException;
import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.enums.RefundStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.order.RefundInfo;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.RefundService;
import com.atguigu.yygh.order.service.WeixinService;
import com.atguigu.yygh.order.utils.ConstantPropertiesUtils;
import com.atguigu.yygh.order.utils.HttpClient;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author nicc
 * @version 1.0
 * @className WeixinServiceImpl
 * @description TODO
 * @date 2022-09-14 10:09
 */
@Service
public class WeixinServiceImpl implements WeixinService {


    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    OrderService orderService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    RefundService refundService;

    @Override
    public Boolean refund(Long orderId) {
        //1、查询支付记录
        PaymentInfo paymentInfo = paymentService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());

        //2、根据支付记录创建退款记录
        RefundInfo refundInfo = refundService.saveRefundInfo(paymentInfo);

        //3、判断退款的状态，如果已经退款  return true
        Integer refundStatus = refundInfo.getRefundStatus();
        if(refundStatus.intValue()== RefundStatusEnum.REFUND.getStatus().intValue()){
            //已经退款
            return true;
        }

        //4、调用微信的退款接口

        try {
            Map<String,String> paramMap = new HashMap<>();

            paramMap.put("appid",ConstantPropertiesUtils.APPID); //公众账号ID
            paramMap.put("mch_id",ConstantPropertiesUtils.PARTNER); //商户编号
            paramMap.put("nonce_str",WXPayUtil.generateNonceStr()); //随机字符串
            paramMap.put("transaction_id",paymentInfo.getTradeNo()); //支付成功后的流水号
            paramMap.put("out_trade_no",paymentInfo.getOutTradeNo()); //商户订单编号
            paramMap.put("out_refund_no","tk"+paymentInfo.getOutTradeNo()); //商户退款单号
            paramMap.put("total_fee","1");//订单金额
            paramMap.put("refund_fee","1");//退款金额


            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            httpClient.setHttps(true);
            httpClient.setCert(true);
            httpClient.setCertPassword(ConstantPropertiesUtils.PARTNER);
            httpClient.setXmlParam(WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY));
            httpClient.post();


            //返回值

            String content = httpClient.getContent();
            Map<String, String> result = WXPayUtil.xmlToMap(content);

            //如果退款成功---》更新退款记录

            if(result!=null && result.get("result_code").equalsIgnoreCase(WXPayConstants.SUCCESS)){
                refundInfo.setTradeNo(result.get("transaction_id"));//退款成功后的流水号
                refundInfo.setCallbackTime(new Date());
                refundInfo.setCallbackContent(result.toString());
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());//2
                refundInfo.setUpdateTime(new Date());
                refundService.updateById(refundInfo);
            }


        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public Map createNative(Long orderId) {

        //1、从redis中根据订单id找到map（链接+其它） ， 如果有就直接return map   --- 五分钟之后使用的是同一个支付链接
        Map map = (Map)redisTemplate.opsForValue().get(orderId.toString());

        if(map!=null){
            return map;
        }

        //2、根据订单id查询订单对象
        OrderInfo orderInfo = orderService.getById(orderId);
        if(orderInfo==null){
            throw new YyghException(20001,"订单不存在");
        }

        //3、为订单创建支付记录（该订单如果没有对应的支付记录则创建）
        paymentService.savePaymentInfo(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());
//        paymentService.savePaymentInfo(orderId);

        //4、调用微信“统一下单”接口

        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("appid", ConstantPropertiesUtils.APPID);//公众账号ID
        paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);//商户号
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串	32位以内
        Date reserveDate = orderInfo.getReserveDate();//安排日期(就诊日期，出诊日期)
        String reserveDateString = new DateTime(reserveDate).toString("yyyy/MM/dd");
        String body = reserveDateString + "就诊"+ orderInfo.getDepname(); // 2022/07/17 就诊 多发性硬化专科门诊
        paramMap.put("body", body);//商品描述
        paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
        //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
        paramMap.put("total_fee", "1");//订单总金额。测试1分钱
        paramMap.put("spbill_create_ip", "127.0.0.1");//终端ip=测试
        paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");//没有
        paramMap.put("trade_type", "NATIVE");//扫码支付，必须写成NATIVE

        try {

            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);//支付https协议
            String xmlParam = WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY);
            httpClient.setXmlParam(xmlParam);//设置参数（微信支付端的接口需要xml格式的参数）
            httpClient.post();//post请求

            // 参数封装  + 发请求  + 返回值 + 解析返回值
            String xmlString = httpClient.getContent();//返回值 --- xml
            Map<String, String> responseMap = WXPayUtil.xmlToMap(xmlString);

            Map<String,Object> result = new HashMap<>();
            result.put("orderId",orderId);
            result.put("totalFee",orderInfo.getAmount());
            result.put("codeUrl",responseMap.get("code_url"));
            result.put("resultCode",responseMap.get("result_code"));

            //存在redis--5分钟
            redisTemplate.opsForValue().set(orderId.toString(),result,5, TimeUnit.MINUTES);

            return result;


        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Map queryPayStatus(Long orderId) {

        //1、查询订单
        OrderInfo orderInfo = orderService.getById(orderId);

        //2、封装请求参数
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("appid",ConstantPropertiesUtils.APPID);
        paramMap.put("mch_id",ConstantPropertiesUtils.PARTNER);
        paramMap.put("out_trade_no",orderInfo.getOutTradeNo());
        paramMap.put("nonce_str",WXPayUtil.generateNonceStr());

        try {

            //3、发请求  微信支付端接口（参数和返回值 xml格式）
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));//设置xml请求参数
            httpClient.post();

            //4、返回值
            String xmlString = httpClient.getContent();
            Map<String, String> result = WXPayUtil.xmlToMap(xmlString);
            // 支付记录的callback_content字段
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

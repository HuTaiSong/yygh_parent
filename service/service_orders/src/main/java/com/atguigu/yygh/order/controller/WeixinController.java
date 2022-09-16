package com.atguigu.yygh.order.controller;

import com.atguigu.rabbit.consts.MqConst;
import com.atguigu.rabbit.service.RabbitService;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.HttpRequestHelper;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.WeixinService;
import com.atguigu.yygh.vo.msm.MsmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author nicc
 * @version 1.0
 * @className WeixinController
 * @description TODO
 * @date 2022-09-15 11:18
 */
@RestController
@RequestMapping("/api/order/weixin")
public class WeixinController {

    @Autowired
    private WeixinService weixinService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private RabbitService rabbitService;

    /**
     * 点击支付按钮之后
     * @param orderId
     * @return
     */
    @GetMapping("/createNative/{orderId}")
    public R createNative(@PathVariable("orderId") Long orderId) {
        //1、redis？ 2、创建支付记录（每一个订单对应一个支付记录） 支付记录的状态=1
        //3、调用微信支付的“统一下单” 接口   4、拿到微信端的返回值   5、封装map    6、 存入redis  7、return map
        Map map = weixinService.createNative(orderId);
        return R.ok().data(map);// orderId totalFee codeUrl resultCode
    }

    /**
     * 查询支付状态  每隔三秒中调用一次
     * @param orderId
     * @return
     */
    @GetMapping("queryPayStatus/{orderId}")
    public R queryPayStatus( @PathVariable("orderId") Long orderId){

        Map map = weixinService.queryPayStatus(orderId);

        if(map==null){
            return R.error().message("支付出错");
        }

        Object trade_state = map.get("trade_state");

        if("SUCCESS".equals(trade_state)){
            //支付成功
            String out_trade_no = (String)map.get("out_trade_no");
            paymentService.paySuccess(out_trade_no,map);



            // 调用医院端接口，修改订单状态
            Map<String, Object> paramMap = new HashMap<>();
            OrderInfo orderInfo = orderService.getById(orderId);
            paramMap.put("hosRecordId",orderInfo.getHosRecordId());//医院端订单id
            HttpRequestHelper.sendRequest(paramMap, "http://localhost:9998/order/updatePayStatus");



            MsmVo msmVo = new MsmVo();
//            OrderInfo orderInfo = orderInfoService.getById(orderId);
            String patientPhone = orderInfo.getPatientPhone();
            msmVo.setPhone(patientPhone);
//            msmVo.setTemplateCode("");
            msmVo.getParam().put("message","支付成功！");

            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM,MqConst.ROUTING_MSM_ITEM,msmVo);

            return R.ok().message("支付成功");
        }
        return R.ok().message("支付中");
    }
}

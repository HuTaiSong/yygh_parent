package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.rabbit.consts.MqConst;
import com.atguigu.rabbit.service.RabbitService;
import com.atguigu.yygh.common.exception.MyException.YyghException;
import com.atguigu.yygh.common.utils.HttpRequestHelper;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.hosp.client.HospitalFeignClient;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.order.mapper.OrderInfoMapper;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.order.service.WeixinService;
import com.atguigu.yygh.user.client.PatientFeignClient;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderCountVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author nicc
 * @version 1.0
 * @className OrderServiceImpl
 * @description TODO
 * @date 2022-09-13 19:11
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {

    @Autowired
    private OrderInfoMapper orderMapper;
    @Autowired
    private PatientFeignClient patientFeignClient;
    @Autowired
    private HospitalFeignClient hospitalFeignClient;
    @Autowired
    private WeixinService weixinService;
    @Autowired
    private RabbitService rabbitService;

    @Override
    public Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo) {
        Map<String, Object> map = new HashMap<>();

        List<OrderCountVo> orderCountVoList
                = baseMapper.selectOrderCount(orderCountQueryVo);
        //日期列表
        List<String> dateList
                =orderCountVoList.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());
        //统计列表
        List<Integer> countList
                =orderCountVoList.stream().map(OrderCountVo::getCount).collect(Collectors.toList());
        map.put("dateList", dateList);
        map.put("countList", countList);
        return map;
    }

    @Override
    public void patientTips() {
        //1、查询今天的挂号订单
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reserve_date",new DateTime().toString("yyyy-MM-dd"));

        List<OrderInfo> orderInfoList = baseMapper.selectList(queryWrapper);

        //2、遍历
        for(OrderInfo orderInfo : orderInfoList) {
            //短信提示
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            msmVo.getParam().put("message","请及时就诊！");

            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }
    }

    @Override
    public Boolean cancelOrder(Long orderId) {
        //完整的退款流程

        //1、查询订单
        OrderInfo orderInfo = baseMapper.selectById(orderId);

        //2、判断 退号时间
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());
        if(quitTime.isBeforeNow()){
//            return false;
            throw new YyghException(20001,"退号时间已过");
        }


        //3、调用医院端退号接口  （1）修改医院端订单的状态status=-1 （2） availableNum+1

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hosRecordId",orderInfo.getHosRecordId());//医院端订单id
        JSONObject jsonObject = HttpRequestHelper.sendRequest(paramMap, "http://localhost:9998/order/updateCancelStatus");

        if(jsonObject.getInteger("code").intValue()==200){
            //医院端取消订单接口调用成功

            //4、判断当前订单的支付状态，如果已支付，退钱
            if(orderInfo.getOrderStatus().intValue()==OrderStatusEnum.PAID.getStatus().intValue()){
                Boolean refund = weixinService.refund(orderId);
                if(!refund){
                    throw new YyghException(20001,"退款失败"); //出现了YyghException类型的异常20001退款失败
                }
            }

            //5、修改平台的订单状态
            orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
            orderInfo.setUpdateTime(new Date());
            baseMapper.updateById(orderInfo);


            //6、号源数量增加 + 就诊人发送短信通知

            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(orderInfo.getScheduleId());//平台端的排班id

            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            msmVo.getParam().put("message","订单取消成功！");

            orderMqVo.setMsmVo(msmVo);

            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER,MqConst.ROUTING_ORDER,orderMqVo);


        }else{
            throw new YyghException(20001,"医院端取消订单接口调用失败");
        }

        return true;
    }

    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo) {
        //orderQueryVo获取条件值
        String name = orderQueryVo.getKeyword(); //医院名称
        Long patientId = orderQueryVo.getPatientId(); //就诊人名称
        String orderStatus = orderQueryVo.getOrderStatus(); //订单状态
        String reserveDate = orderQueryVo.getReserveDate();//安排时间
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();
        Long userId = orderQueryVo.getUserId();

        //对条件值进行非空判断
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();

        if(!StringUtils.isEmpty(userId)) {
            wrapper.eq("user_id",userId);
        }
        if(!StringUtils.isEmpty(name)) {
            wrapper.like("hosname",name);
        }
        if(!StringUtils.isEmpty(patientId)) {
            wrapper.eq("patient_id",patientId);
        }
        if(!StringUtils.isEmpty(orderStatus)) {
            wrapper.eq("order_status",orderStatus);
        }
        if(!StringUtils.isEmpty(reserveDate)) {
            wrapper.ge("reserve_date",reserveDate);
        }
        if(!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time",createTimeEnd);
        }

        //调用mapper的方法
        IPage<OrderInfo> pages = baseMapper.selectPage(pageParam, wrapper);

        //编号变成对应值封装
        pages.getRecords().stream().forEach(item -> {
            this.packOrderInfo(item);
        });
        return pages;
    }

    @Override
    public OrderInfo getOrderInfo(Long id) {
        OrderInfo orderInfo = baseMapper.selectById(id);
//        Integer orderStatus = orderInfo.getOrderStatus();//订单状态
        return this.packOrderInfo(orderInfo);
    }

    private OrderInfo packOrderInfo(OrderInfo orderInfo) {
        String orderStatusString = OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus());
        orderInfo.getParam().put("orderStatusString",orderStatusString);
        return orderInfo;
    }

    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        //1、获取创建订单需要数据
        Patient patient = patientFeignClient.getPatientById(patientId);
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);


        //2、调用医院端的接口
        //  2.1  封装请求参数 （医院端需要什么，我们就给传递对应参数）
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",scheduleOrderVo.getHoscode());
        paramMap.put("depcode",scheduleOrderVo.getDepcode());
        paramMap.put("hosScheduleId",scheduleOrderVo.getHosScheduleId());
        paramMap.put("reserveDate",new DateTime(scheduleOrderVo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", scheduleOrderVo.getReserveTime());
        paramMap.put("amount",scheduleOrderVo.getAmount()); //挂号费用
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType",patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex",patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone",patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode",patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode",patient.getDistrictCode());
        paramMap.put("address",patient.getAddress());
        paramMap.put("contactsName",patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo",patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone",patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        paramMap.put("sign", "");


        // 2.2  发请求 -- 调用医院端接口（  ）
        String url = "http://localhost:9998/order/submitOrder";
        //注意：医院工程必须启动起来
        JSONObject jsonObject = HttpRequestHelper.sendRequest(paramMap, url);


        if(jsonObject.getInteger("code").intValue()==200){
            //正常返回

            JSONObject data = jsonObject.getJSONObject("data");

            Long hosRecordId = data.getLong("hosRecordId");//医院端订单的主键id
            Integer number = data.getInteger("number");//取号顺序
            String fetchTime = data.getString("fetchTime");//取号时间
            String fetchAddress = data.getString("fetchAddress");//取号地点

            //3、创建平台自己的订单

            OrderInfo orderInfo = new OrderInfo();

            BeanUtils.copyProperties(scheduleOrderVo, orderInfo);
            String outTradeNo = System.currentTimeMillis() + "" + new Random().nextInt(100);
            orderInfo.setOutTradeNo(outTradeNo);//订单编号
            orderInfo.setScheduleId(scheduleId);//平台的排班id
            orderInfo.setUserId(patient.getUserId());//用户id
            orderInfo.setPatientId(patientId);//就诊人id
            orderInfo.setPatientName(patient.getName());
            orderInfo.setPatientPhone(patient.getPhone());
            orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());//订单状态  status=0
            orderInfo.setHosRecordId(hosRecordId+"");// 平台端订单，存储医院端订单的id
            orderInfo.setNumber(number);//取号顺序
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);

            baseMapper.insert(orderInfo);


            //todo 将来想更新到mongodb中去
            Integer reservedNumber = data.getInteger("reservedNumber");//号总数
            Integer availableNumber = data.getInteger("availableNumber");//医院端剩余号数

            this.afterSaveOrder(reservedNumber,availableNumber,scheduleId,orderInfo);


            return orderInfo.getId();
        }else{
            //调用医院端接口失败
            throw new YyghException(20001,"医院端接口调用失败（有可能医院端没启动）");
        }
    }

    private void afterSaveOrder(Integer reservedNumber, Integer availableNumber, String scheduleId, OrderInfo orderInfo) {
        //   同步         scheduleFeign.updateSchedule(reservedNumber,availableNumber,scheduleId);
        //   异步  发送mq消息 （时间可以忽略不计）   降低整体的响应时间，提供系统的吞吐量

        //1、封装消息
        OrderMqVo orderMqVo = new OrderMqVo();
        orderMqVo.setAvailableNumber(availableNumber);
        orderMqVo.setReservedNumber(reservedNumber);
        orderMqVo.setScheduleId(scheduleId);//mongodb排班id


        MsmVo msmVo = new MsmVo();
        msmVo.setPhone(orderInfo.getPatientPhone());//就诊人手机号
        msmVo.setTemplateCode("短信模板");
        msmVo.getParam().put("message","【尚医通】挂号成功！");//message就是发送的内容

        orderMqVo.setMsmVo(msmVo);

        //发送mq消息()
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER,MqConst.ROUTING_ORDER,orderMqVo);
    }
}

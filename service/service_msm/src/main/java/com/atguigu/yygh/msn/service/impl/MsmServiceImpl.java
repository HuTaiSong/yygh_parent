package com.atguigu.yygh.msn.service.impl;

import com.atguigu.yygh.msn.service.MsmService;
import com.atguigu.yygh.msn.util.HttpUtils;
import org.apache.http.HttpResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author nicc
 * @version 1.0
 * @className MsmServiceImpl
 * @description TODO
 * @date 2022-09-07 15:06
 */
@Service
public class MsmServiceImpl implements MsmService {



    @Override
    public boolean send(String phone, String code) {
        String host = "http://dingxin.market.alicloudapi.com";
        String path = "/dx/sendSms";
        String method = "POST";
        String appcode = "写成你自己的appcode";
        Map<String, String> headers = new HashMap<String, String>();
        //Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);
        querys.put("param", "code:" + code);
        querys.put("tpl_id", "TP1711063");
        Map<String, String> bodys = new HashMap<String, String>();

        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}

package com.atguigu.yygh.user.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.utils.ConstantPropertiesUtil;
import com.atguigu.yygh.user.utils.HttpClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author nicc
 * @version 1.0
 * @className WxApiController
 * @description TODO
 * @date 2022-09-07 18:56
 */
@Controller
@RequestMapping("/api/user/wx")
public class WxApiController {

    @Autowired
    private UserInfoService userInfoService;

    //@Autowired
    //private RedisTemplate redisTemplate;

    /**
     * 获取微信登录参数
     */
    @ResponseBody
    @GetMapping("getLoginParam")
    public R getLoginParam() throws Exception{

        String appid = ConstantPropertiesUtil.WX_OPEN_APP_ID;
        String scope = "snsapi_login";
        String redirectUri = URLEncoder.encode(ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL, "utf-8");
        String state = System.currentTimeMillis()+"";

        Map<String,Object> map = new HashMap<>();
        map.put("appid",appid);
        map.put("scope",scope);
        map.put("redirectUri",redirectUri);
        map.put("state",state);

        return R.ok().data(map);
    }

    @GetMapping("/callback")
    public String callback(String code) {
        try {
            //1.得到授权临时票据code
            System.out.println("code = " + code);

            //2.根据临时票据获取access_token
            String accessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
                    "appid=" + ConstantPropertiesUtil.WX_OPEN_APP_ID + "" +
                    "&secret=" + ConstantPropertiesUtil.WX_OPEN_APP_SECRET + "" +
                    "&code=" + code +
                    "&grant_type=authorization_code";

            String s = HttpClientUtils.get(accessTokenUrl, "utf-8");
            JSONObject jsonObject = JSONObject.parseObject(s);

            //3.获取返回值
            String access_token = jsonObject.getString("access_token");
            String openid = jsonObject.getString("openid");

            //4.根据openid查询user_info表中是否存在当前微信用户
            UserInfo userInfo = userInfoService.findWeiXinByOpenid(openid);

            //若不存在当前微信用户
            if (userInfo == null) {
                userInfo = new UserInfo();
                //5.根据access_token=openid获取微信昵称
                String newUrl = "https://api.weixin.qq.com/sns/userinfo?" +
                        "access_token=" + access_token + "&openid=" + openid;
                String s1 = HttpClientUtils.get(newUrl);
                JSONObject jsonObject1 = JSONObject.parseObject(s1);

                //昵称
                String nickname = jsonObject1.getString("nickname");

                userInfo.setOpenid(openid);
                userInfo.setNickName(nickname);

                //新增微信用户信息
                userInfoService.save(userInfo);
            }

            //获取name
            String name = userInfo.getName();
            if (StringUtils.isEmpty(name)) {
                name = userInfo.getNickName();//若name为空，用nickName替代
                if (StringUtils.isEmpty(name)) {
                    name = userInfo.getPhone();//若nickName为空，用phone替代
                }
            }

            //jwt令牌
            String token = JwtHelper.createToken(userInfo.getId(), name);

            name = URLEncoder.encode(name, "utf-8");
            String phone = userInfo.getPhone();


            if (StringUtils.isEmpty(phone)) {
                //微信登陆后绑定手机号
                return "redirect:http://localhost:3000/weixin/callback?name=" + name + "&token=" + token + "&openid=" + userInfo.getOpenid();
            } else {
                //不需要绑定手机号
                return "redirect:http://localhost:3000/weixin/callback?name=" + name + "&token=" + token + "&openid=";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

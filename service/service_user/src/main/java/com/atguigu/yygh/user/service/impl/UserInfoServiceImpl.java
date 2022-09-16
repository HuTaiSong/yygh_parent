package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.common.exception.MyException.YyghException;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nicc
 * @version 1.0
 * @className UserInfoServiceImpl
 * @description TODO
 * @date 2022-09-06 15:02
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private PatientService patientService;

    @Override
    public void approval(Long userId, Integer authStatus) {
        if(authStatus.intValue()==2 || authStatus.intValue()==-1) {
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }
    }

    @Override
    public Map<String, Object> show(Long userId) {
        HashMap<String, Object> map = new HashMap<>();
        //根据userId查询信息
        UserInfo userInfo = this.packUserInfo(this.getById(userId));
        //查询userId对应的就诊人列表
        List<Patient> patientList = patientService.findByUserId(userId);

        //封装map
        map.put("userInfo", userInfo);
        map.put("patientList",patientList);
        return map;
    }

    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        //1、loginVo 获取出 传过来的 手机号 + 验证码
        String phone = loginVo.getPhone();//手机号
        String code = loginVo.getCode();//页面输入的验证码

        //2、参数校验，例如：是否为空
        if(StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            throw new YyghException(20001,"数据为空");
        }

        //3、 TODO 校验  验证码是否正确  失败 --> 异常
        //  code.eq(xxx)             13104405678----1234
        //校验校验验证码
        String mobileCode = redisTemplate.opsForValue().get(phone);
        if(!code.equals(mobileCode)) {
            throw new YyghException(20001,"验证码失败");
        }

        //直接通过手机号+验证
        Map<String, Object> map = new HashMap<>();

        if(StringUtils.isEmpty(loginVo.getOpenid())){
            //4、判断该手机号在user_info表中是否存在
            QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("phone", phone);
            UserInfo userInfo = baseMapper.selectOne(queryWrapper);

            //5、如果不存在 ---> 自动注册
            if(null == userInfo) {
                userInfo = new UserInfo();

                userInfo.setPhone(phone);
                userInfo.setStatus(1);//0：锁定 1：正常

                this.save(userInfo);//注册
            }

            //6、判断用户的状态   status=0  锁定  （异常），   status=1 正常
            if(userInfo.getStatus() == 0) {
                throw new YyghException(20001,"用户已经禁用");
            }

            //7.返回 name + token
            map = this.get(userInfo);

        } else {
            String openid = loginVo.getOpenid();

            // 根据openid查询---》userInfo
            UserInfo userInfo_wx = this.findWeiXinByOpenid(openid);
            QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("phone",loginVo.getPhone());

            UserInfo userInfo_phone = baseMapper.selectOne(queryWrapper);

            //判断是否绑定电话
            if(userInfo_phone!=null){
                //合并
                userInfo_wx.setPhone(userInfo_phone.getPhone());
                userInfo_wx.setName(userInfo_phone.getName());
                userInfo_wx.setCertificatesType(userInfo_phone.getCertificatesType());
                userInfo_wx.setCertificatesNo(userInfo_phone.getCertificatesNo());
                userInfo_wx.setCertificatesUrl(userInfo_phone.getCertificatesUrl());
                userInfo_wx.setAuthStatus(userInfo_phone.getAuthStatus());
                userInfo_wx.setStatus(userInfo_phone.getStatus());

//                baseMapper.deleteById(userInfo_phone.getId());//或者根据手机号去删除
                baseMapper.delete(new QueryWrapper<UserInfo>().eq("phone",loginVo.getPhone()));

                baseMapper.updateById(userInfo_wx);

            }else {
//                userInfo_phone     没有查询到
                userInfo_wx.setPhone(loginVo.getPhone());
                baseMapper.updateById(userInfo_wx);
            }
            map = this.get(userInfo_wx);
        }

        return map;
    }

    public Map get(UserInfo userInfo){
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();//用户的真实姓名（用户认证）
        if(StringUtils.isEmpty(name)){
            name = userInfo.getNickName();//微信昵称
            if(StringUtils.isEmpty(name)){
                name = userInfo.getPhone();
            }
        }
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token",token);
        map.put("name",name);
        return map;
    }

    @Override
    public UserInfo findWeiXinByOpenid(String openid) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper();
        queryWrapper.eq("openid", openid);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //根据用户id查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);
        //设置认证信息
        //认证人姓名
        userInfo.setName(userAuthVo.getName());
        //其他认证信息
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());

        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        //进行信息更新
        baseMapper.updateById(userInfo);
    }

    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo) {
        String keyword = userInfoQueryVo.getKeyword();//关键词   用户名模糊查询
        Integer status = userInfoQueryVo.getStatus();
        Integer authStatus = userInfoQueryVo.getAuthStatus();

        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin();
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd();

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();

        if(!StringUtils.isEmpty(keyword)){
            queryWrapper.like("name",keyword);
            queryWrapper.or().like("phone",keyword);
//            or   phone like keyword
        }

        if(!StringUtils.isEmpty(status)){
            queryWrapper.eq("status",status);
        }

        if(!StringUtils.isEmpty(authStatus)){
            queryWrapper.eq("auth_status",authStatus);
        }

        if(!StringUtils.isEmpty(createTimeBegin)){
            queryWrapper.ge("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)){
            queryWrapper.le("create_time",createTimeEnd);
        }

        Page<UserInfo> userInfoPage = baseMapper.selectPage(pageParam, queryWrapper);

        userInfoPage.getRecords().forEach(userInfo -> {
            this.packUserInfo(userInfo);
        });

        return userInfoPage;
    }

    @Override
    public void lock(Long userId, Integer status) {

        if(status.intValue() == 0 || status.intValue() == 1) {
            UserInfo userInfo = this.getById(userId);
            userInfo.setStatus(status);
            this.updateById(userInfo);
        }
    }

    private UserInfo packUserInfo(UserInfo userInfo) {
        Integer status = userInfo.getStatus();
        Integer authStatus = userInfo.getAuthStatus();

//        AuthStatusEnum[] values = AuthStatusEnum.values();
        String statusNameByStatus = AuthStatusEnum.getStatusNameByStatus(authStatus);

        userInfo.getParam().put("statusString",status==0?"锁定":"正常");
        userInfo.getParam().put("authStatusString",statusNameByStatus);

        return userInfo;
    }

}

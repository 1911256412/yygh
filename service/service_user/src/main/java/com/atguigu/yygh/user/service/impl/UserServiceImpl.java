package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.handler.YyghException;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.result.Result;
import com.atguigu.yygh.result.ResultCodeEnum;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.atguigu.yygh.user.service.UserService;
import com.atguigu.yygh.utils.JwtHelper;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private PatientService patientService;

    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        //判断手机号验证码是否为空
        if (StringUtils.isEmpty(loginVo.getPhone()) || StringUtils.isEmpty(loginVo.getPhone())) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //判断输入的验证码和短信发送的验证码是否一致
        //查询redis数据库 中短信接口存到redis中的数据
        String s = redisTemplate.opsForValue().get(loginVo.getPhone());
        if (!loginVo.getCode().equals(s)) {
            //如果验证码不相等 则抛出异常
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }
        //判断是否有手机号，如果有手机号把手机号添加到数据库中
        UserInfo userInfo = null;
        if (!StringUtils.isEmpty(loginVo.getOpenid())) {
            userInfo = this.getByOpenid(loginVo.getOpenid());
            if (null != userInfo) {
                userInfo.setPhone(loginVo.getPhone());
                this.updateById(userInfo);
            } else {
                throw new YyghException(ResultCodeEnum.DATA_ERROR);
            }
        }
        //userInfo=null 说明手机直接登录
        if (null == userInfo) {

            //判断是否是第一次登录 ，如果是第一次登录，数据库进行注册
            LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserInfo::getPhone, loginVo.getPhone());

            userInfo = baseMapper.selectOne(wrapper);

            if (userInfo == null) {
                userInfo = new UserInfo();
                userInfo.setName("");
                userInfo.setPhone(loginVo.getPhone());
                userInfo.setStatus(1);
                baseMapper.insert(userInfo);
            }
        }
        Map<String, Object> map = new HashMap<>();
        //校验是否被禁用
        if (userInfo.getStatus() == 0) {
            throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }
        String name = userInfo.getName();
        //如果名称等于空
        if (!StringUtils.isEmpty(name)) {
            //返回昵称
            name = userInfo.getNickName();
        }
        if (StringUtils.isEmpty(name)) {
            //返回手机号
            name = userInfo.getPhone();
        }
        map.put("name", name);
        //token生成
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);
        return map;
    }

    private UserInfo getByOpenid(String openid) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getOpenid, openid);

        UserInfo userInfo = baseMapper.selectOne(wrapper);
        return userInfo;
    }

    @Override
    public UserInfo selectByOpenId(String open) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getOpenid, open);
        UserInfo userInfo = baseMapper.selectOne(wrapper);
        return userInfo;
    }

    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //通过id查询userinfo
        UserInfo userInfo = baseMapper.selectById(userId);
        userInfo.setName(userAuthVo.getName());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        int i = baseMapper.updateById(userInfo);
    }

    @Override
    public Page<UserInfo> selectByPage(Page<UserInfo> page, UserInfoQueryVo userInfoQueryVo) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        Integer authStatus = userInfoQueryVo.getAuthStatus();

        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin();
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd();
        String name = userInfoQueryVo.getKeyword();
        Integer status = userInfoQueryVo.getStatus();
        if (!StringUtils.isEmpty(name)) {
            wrapper.like(UserInfo::getName, name);
        }
        if (authStatus!=null) {
            wrapper.eq(UserInfo::getAuthStatus, authStatus);
        }
        if (status!=null) {
            wrapper.eq(UserInfo::getStatus, status);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge(UserInfo::getCreateTime, createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.ge(UserInfo::getCreateTime, createTimeEnd);
        }
        Page<UserInfo> userInfoPage = baseMapper.selectPage(page, wrapper);
        userInfoPage.getRecords().stream().forEach(item -> {
            this.Pack(item);
        });
        return userInfoPage;
    }

    @Override
    public boolean lock(Long userId, Integer status) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        UserInfo userInfo = baseMapper.selectById(userId);
        if (status.intValue() == 0 || status.intValue() == 1) {
            userInfo.setStatus(status);
            baseMapper.updateById(userInfo);
        }
        return true;
    }

    @Override
    public Map<String, Object> selectByuserId(Long userId) {
        UserInfo userInfo = baseMapper.selectById(userId);
        this.Pack(userInfo);
        Map<String, Object> map = new HashMap<>();
        map.put("userInfo", userInfo);
        //查询就诊人信息
        List<Patient> patients = patientService.findAllPatient(userId);
        map.put("patientList", patients);
        return map;
    }

    @Override
    public void approval(Long userId, Integer authStatus) {
        if (authStatus.intValue() == 2 || authStatus.intValue() == -1) {
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }
    }

    private void Pack(UserInfo item) {
        item.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(item.getAuthStatus()));
        String statusString = item.getStatus().intValue() == 0 ? "锁定" : "正常";
        item.getParam().put("statusString", statusString);
    }
}

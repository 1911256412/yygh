package com.atguigu.yygh.user.service;

import com.atguigu.yygh.model.acl.User;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface UserService extends IService<UserInfo> {
    Map<String, Object> login(LoginVo loginVo);

    UserInfo selectByOpenId(String open);

    void userAuth(Long userId, UserAuthVo userAuthVo);

    Page<UserInfo> selectByPage(Page<UserInfo> page, UserInfoQueryVo userInfoQueryVo);

    boolean lock(Long userId, Integer status);

    Map<String, Object> selectByuserId(Long userId);

    void approval(Long userId, Integer authStatus);
}

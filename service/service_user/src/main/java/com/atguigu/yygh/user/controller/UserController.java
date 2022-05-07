package com.atguigu.yygh.user.controller;

import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.result.Result;
import com.atguigu.yygh.user.service.UserService;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags = "后台用户管理")
@RequestMapping("/admin/user")
public class UserController {
    @Resource
    private UserService userService;

    @ApiOperation("条件查询带分页")
    @GetMapping("{current}/{limit}")
    public Result list(@PathVariable  Long current,
                       @PathVariable  Long limit,
                       UserInfoQueryVo userInfoQueryVo){
        Page<UserInfo> page=new Page<UserInfo>(current,limit);
        Page<UserInfo> list=userService.selectByPage(page,userInfoQueryVo);
        return Result.ok(list);
    }
    @ApiOperation("用户锁定")
    @PutMapping("lock/{userId}/{status}")
    public Result lock(@PathVariable Long userId,
                       @PathVariable Integer status){
        boolean b=userService.lock(userId,status);
        return Result.ok();
    }
    @ApiOperation("根据用户id查询用户详情")
    @GetMapping("getInfo/{userId}")
    public Result selectInfo(@PathVariable  Long userId){
        Map<String,Object> map = userService.selectByuserId(userId);
        return Result.ok(map);
    }
    @ApiOperation("用户审批")
    @PostMapping("approval/{userId}/{authStatus}")
    public Result approval(@PathVariable  Long userId,
                           @PathVariable  Integer authStatus){
        userService.approval(userId,authStatus);

        return Result.ok();
    }
}

package com.atguigu.yygh.user.api;

import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.result.Result;
import com.atguigu.yygh.user.service.UserService;
import com.atguigu.yygh.utils.JwtHelper;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserInfoController {
    @Resource
    private UserService userService;

    @PostMapping("login")
    public Result  Login(@RequestBody LoginVo loginVo){
        Map<String,Object> map =userService.login(loginVo);
        return Result.ok(map );
    }
    //用户认证
    @ApiOperation("用户认证")
    @PostMapping("auth/userAuth")
    public Result userAuth(HttpServletRequest request,@RequestBody UserAuthVo userAuthVo){
        //传递用户id 和 用户认证信息
        Long userId = JwtHelper.getUserId(request.getHeader("token"));
        userService.userAuth(userId,userAuthVo);
        return Result.ok();
    }
    //根据用户id获取用户信息
    @ApiOperation("根据用户ID获取用户信息")
    @GetMapping("auth/getUserInfo")
    public Result getUserInfo(HttpServletRequest request){
        Long userId = JwtHelper.getUserId(request.getHeader("token"));
        UserInfo byId = userService.getById(userId);
        return Result.ok(byId);
    }
}

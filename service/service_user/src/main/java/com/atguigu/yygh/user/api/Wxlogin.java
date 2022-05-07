package com.atguigu.yygh.user.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.handler.YyghException;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.result.Result;
import com.atguigu.yygh.result.ResultCodeEnum;
import com.atguigu.yygh.user.config.ConstantPropertiesUtil;
import com.atguigu.yygh.user.service.UserService;
import com.atguigu.yygh.utils.HttpClientUtils;
import com.atguigu.yygh.utils.JwtHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/ucenter/wx")
@Api(tags = "微信操作")
@Slf4j
public class Wxlogin {

    @Resource
    private UserService userService;

    @ApiOperation("返回生成微信二维码的参数")
    @GetMapping("getLoginParam")
    @ResponseBody
    public Result createWX() {
        Map<String, Object> map = new HashMap<>();
        try {
            String redirectUri = URLEncoder.encode(ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL, "UTF-8");
            map.put("appid", ConstantPropertiesUtil.WX_OPEN_APP_ID);
            map.put("redirectUri", redirectUri);
            map.put("scope", "snsapi_login");
            map.put("state", System.currentTimeMillis() + "");//System.currentTimeMillis()+""
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Result.ok(map);
    }
    //微信扫码之后回调的方法
    @ApiOperation("微信扫码之后回调的方法")
    @GetMapping("callback")
    public String  Callback(String code ,String state){
//获取授权临时票据
        if (StringUtils.isEmpty(state) || StringUtils.isEmpty(code)) {

            throw new YyghException(ResultCodeEnum.ILLEGAL_CALLBACK_REQUEST_ERROR);
        }
        //使用code和appid以及appscrect换取access_token
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");

        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                ConstantPropertiesUtil.WX_OPEN_APP_ID,
                ConstantPropertiesUtil.WX_OPEN_APP_SECRET,
                code);

        String result = null;
        try {
            //得到access_token的值
            result = HttpClientUtils.get(accessTokenUrl);

        } catch (Exception e) {
            throw new YyghException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }
        //2、 根据access_token的值访问微信的固定地址
        JSONObject jsonObject = JSONObject.parseObject(result);
        String access_token = (String) jsonObject.get("access_token");
        String openid = (String )jsonObject.get("openid");
        String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                "?access_token=%s" +
                "&openid=%s";
        String userInfoUrl = String.format(baseUserInfoUrl, access_token, openid);
        String resultUserInfo = null;
        try {
            resultUserInfo = HttpClientUtils.get(userInfoUrl);
        } catch (Exception e) {
            throw new YyghException(ResultCodeEnum.FETCH_USERINFO_ERROR);
        }
        JSONObject user=JSONObject.parseObject(resultUserInfo);
        String open = (String )user.get("openid");
        String nickname = (String )user.get("nickname");
        //根据openid查询数据库看数据库中有没有 ，
        UserInfo userInfo=userService.selectByOpenId(open);
        if(userInfo==null){
            //把用户的信息存到数据库中;
            userInfo=new UserInfo();
            userInfo.setNickName(nickname);
            userInfo.setOpenid(open);
            userInfo.setStatus(1);
            userService.save(userInfo);
        }
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);
        if(StringUtils.isEmpty(userInfo.getPhone())) {
            map.put("openid", userInfo.getOpenid());
        } else {
            map.put("openid", "");
        }
        String token = JwtHelper.createToken(userInfo.getId(), name);

        return "redirect:" + ConstantPropertiesUtil.YYGH_BASE_URL + "/weixin/callback?token="+map.get("token")+"&openid="+map.get("openid")+"&name="+URLEncoder.encode((String)map.get("name"));
    }

}


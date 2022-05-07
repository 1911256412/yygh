package com.atguigu.yygh.msm.service.impl;

import com.atguigu.yygh.handler.YyghException;
import com.atguigu.yygh.msm.service.MsmService;
import com.atguigu.yygh.msm.util.HttpUtils;

import com.atguigu.yygh.vo.msm.MsmVo;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;

import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MsmServiceImpl implements MsmService {
    @Override
    public boolean send(String phone, String code) {

        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String method = "POST";
        String appcode = "12511f3fe9604158b3efc6e09d516dc6";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);
        querys.put("param", "**code**:"+code+",**minute**:5");
        querys.put("smsSignId", "2e65b1bb3d054466b82f0c9d125465e2");
        querys.put("templateId", "908e94ccf08b4476ba6c876d13f084ad");
        Map<String, String> bodys = new HashMap<String, String>();
        try {
           // System.out.println("param +++++++++"+querys.get("param").toString());
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
             HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);

           // System.out.println("response"+response.toString());
            //获取response的body
            //System.out.println("responsebody"+ EntityUtils.toString(response.getEntity()));
            return true;
        }    catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean send(MsmVo msmVo) {
        System.out.println("手机号********"+msmVo.getPhone());
        System.out.println(msmVo.getParam().get("code"));
        this.send(msmVo.getPhone(),"1234");
//        if(!StringUtils.isEmpty(msmVo.getPhone())){
//            String code = (String) msmVo.getParam().get("code");
//            System.out.println("***************** 发送短信 *****************");
//            return this.send(msmVo.getPhone(),code);
//        }
        return false;
    }
}

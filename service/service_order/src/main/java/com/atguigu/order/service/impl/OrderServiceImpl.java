package com.atguigu.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.UserFeiginClient;
import com.atguigu.client.HosptialFeiginClient;
import com.atguigu.order.mapper.OrderMapper;
import com.atguigu.order.service.OrderService;
import com.atguigu.rabbitmq.common.constant.MqConst;
import com.atguigu.rabbitmq.service.RabbitService;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.handler.YyghException;
import com.atguigu.yygh.helper.HttpRequestHelper;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.result.ResultCodeEnum;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderCountVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import com.atguigu.yygh.vo.order.SignInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderInfo> implements OrderService {

    @Resource
    private UserFeiginClient userFeiginClient;
    @Resource
    private HosptialFeiginClient hosptialFeiginClient;

    @Resource
    private RabbitService rabbitService;

    @Resource
    private OrderMapper orderMapper;

    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        Patient patientOrder = userFeiginClient.getPatientOrder(patientId);
        ScheduleOrderVo scheduleOrderVo = hosptialFeiginClient.getScheduleOrderVo(scheduleId);
        if (null == patientOrder) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        if (null == scheduleOrderVo) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        //当前时间不可以预约
        if (new DateTime(scheduleOrderVo.getStartTime()).isAfterNow()
                || new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()) {
            throw new YyghException(ResultCodeEnum.TIME_NO);
        }
        SignInfoVo signInfoVo = hosptialFeiginClient.getSignInfoVo(scheduleOrderVo.getHoscode());
        if (null == scheduleOrderVo) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        if (scheduleOrderVo.getAvailableNumber() <= 0) {
            throw new YyghException(ResultCodeEnum.NUMBER_NO);
        }
        OrderInfo orderInfo = new OrderInfo();
        BeanUtils.copyProperties(scheduleOrderVo, orderInfo);
        String outTradeNo = System.currentTimeMillis() + "" + new Random().nextInt(100);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setScheduleId(scheduleId);
        orderInfo.setUserId(patientOrder.getUserId());
        orderInfo.setPatientId(patientId);
        orderInfo.setPatientName(patientOrder.getName());
        orderInfo.setPatientPhone(patientOrder.getPhone());
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
        this.save(orderInfo);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode", orderInfo.getHoscode());
        paramMap.put("depcode", orderInfo.getDepcode());
        paramMap.put("hosScheduleId", orderInfo.getScheduleId());
        paramMap.put("reserveDate", new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", orderInfo.getReserveTime());
        paramMap.put("amount", orderInfo.getAmount());
        paramMap.put("name", patientOrder.getName());
        paramMap.put("certificatesType", patientOrder.getCertificatesType());
        paramMap.put("certificatesNo", patientOrder.getCertificatesNo());
        paramMap.put("sex", patientOrder.getSex());
        paramMap.put("birthdate", patientOrder.getBirthdate());
        paramMap.put("phone", patientOrder.getPhone());
        paramMap.put("isMarry", patientOrder.getIsMarry());
        paramMap.put("provinceCode", patientOrder.getProvinceCode());
        paramMap.put("cityCode", patientOrder.getCityCode());
        paramMap.put("districtCode", patientOrder.getDistrictCode());
        paramMap.put("address", patientOrder.getAddress());
        //联系人
        paramMap.put("contactsName", patientOrder.getContactsName());
        paramMap.put("contactsCertificatesType", patientOrder.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo", patientOrder.getContactsCertificatesNo());
        paramMap.put("contactsPhone", patientOrder.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        paramMap.put("sign", sign);
        JSONObject result = HttpRequestHelper.sendRequest(paramMap, signInfoVo.getApiUrl() + "/order/submitOrder");

        if (result.getInteger("code") == 200) {
            JSONObject jsonObject = result.getJSONObject("data");
            //预约记录唯一标识（医院预约记录主键）
            String hosRecordId = jsonObject.getString("hosRecordId");
            //预约序号
            Integer number = jsonObject.getInteger("number");
            ;
            //取号时间
            String fetchTime = jsonObject.getString("fetchTime");
            ;
            //取号地址
            String fetchAddress = jsonObject.getString("fetchAddress");
            ;
            //更新订单
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);
            baseMapper.updateById(orderInfo);
            Integer reservedNumber = jsonObject.getInteger("reservedNumber");
            //排班剩余预约数
            Integer availableNumber = jsonObject.getInteger("availableNumber");
            //发送mq信息更新号源和短信通知
            //发送mq信息更新号源和短信通知
            //发送mq信息更新号源
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(scheduleId);
            orderMqVo.setReservedNumber(reservedNumber);
            orderMqVo.setAvailableNumber(availableNumber);

            //短信提示
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            msmVo.setTemplateCode("SMS_194640721");
            String reserveDate =
                    new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                            + (orderInfo.getReserveTime() == 0 ? "上午" : "下午");
            Map<String, Object> param = new HashMap<String, Object>() {{
                put("title", orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle());
                put("amount", orderInfo.getAmount());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
                put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
            }};
            msmVo.setParam(param);
            orderMqVo.setMsmVo(msmVo);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);

        } else {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
        return orderInfo.getId();
    }


    public String patientTips() {
        System.out.println("*****PatientTipsService");

//        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("reserve_date",new DateTime().toString("yyyy-MM-dd"));
//        List<OrderInfo> orderInfoList = baseMapper.selectList(queryWrapper);
//        for(OrderInfo orderInfo : orderInfoList) {
//            //短信提示
        MsmVo msmVo = new MsmVo();
//            msmVo.setPhone(orderInfo.getPatientPhone());
//            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime()==0 ? "上午": "下午");
//            Map<String,Object> param = new HashMap<String,Object>(){{
//                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
//                put("reserveDate", reserveDate);
//                put("name", orderInfo.getPatientName());
//            }};
//            msmVo.setParam(param);
        msmVo.setPhone("17645534657");
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("code", "1234");
        msmVo.setParam(param);
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        return "1";
    }

    @Override
    public Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo) {
        List<OrderCountVo> orderCountVoList = orderMapper.getCountMap(orderCountQueryVo);
        //统计列表
        List<Integer> countList = orderCountVoList.stream().map(OrderCountVo::getCount).collect(Collectors.toList());
        //日期列表
        List<String> ReserveDateList = orderCountVoList.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("dateList", ReserveDateList);
        map.put("countList", countList);
        return map;
    }
}

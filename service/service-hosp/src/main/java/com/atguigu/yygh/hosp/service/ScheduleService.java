package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ScheduleService extends IService<Schedule> {
    void saveSchedule(Map<String, Object> map);

    Page<Schedule> selectBypage(int page, int limit, ScheduleQueryVo scheduleQueryVo);

    void remove(String hoscode, String hosScheduleId);

    Map<String, Object> selectByCondiction(Integer current, Integer limit, String hoscode, String depcode);

    List<Schedule> getScheduleDeatil(String hoscode, String depcode, String workDate);

    Map<String,Object>  getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode);

    List<Schedule> getShcedule(String hoscode, String depcode, String workDate);

    Schedule  getById(String scheduleId);

    ScheduleOrderVo getScheduleOrderVo(String scheduleId);
    void update (Schedule schedule);
}

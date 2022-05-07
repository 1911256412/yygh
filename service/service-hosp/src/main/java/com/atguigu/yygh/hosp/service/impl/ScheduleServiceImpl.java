package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.handler.YyghException;
import com.atguigu.yygh.hosp.mapper.ScheduleMapper;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;

import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.result.ResultCodeEnum;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper,Schedule>implements ScheduleService  {

    @Resource
    private ScheduleRepository scheduleRepository;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private HospitalService hospitalService;

    @Resource
    private DepartmentService departmentService;

    @Override
    public void saveSchedule(Map<String, Object> map) {
        String s = JSONObject.toJSONString(map);
        Schedule schedule = JSONObject.parseObject(s, Schedule.class);
        //判断是是否有数据
        Schedule targetSchedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(), schedule.getHosScheduleId());
        if (targetSchedule != null) {
            BeanUtils.copyProperties(schedule, targetSchedule);
            scheduleRepository.save(targetSchedule);
        } else {
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);

        }
    }

    @Override
    public Page<Schedule> selectBypage(int page, int limit, ScheduleQueryVo scheduleQueryVo) {

        Pageable pageable = PageRequest.of(page - 1, limit);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()//构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);//改变默认字符串匹配方式
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo, schedule);
        schedule.setIsDeleted(0);
        Example<Schedule> example = Example.of(schedule, exampleMatcher);
        Page<Schedule> all = scheduleRepository.findAll(example, pageable);
        return all;
    }

    @Override
    public void remove(String hoscode, String hosScheduleId) {
        Schedule targetSchedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (null != targetSchedule) {
            scheduleRepository.deleteById(targetSchedule.getId());
        }

    }

    @Override
    public Map<String, Object> selectByCondiction(Integer current, Integer limit, String hoscode, String depcode) {
        //1、根据医院编号，科室，按照条件查询，封装条件
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        //2、根据工作日workDate进行分组，
        Aggregation agg = Aggregation.newAggregation(
                //匹配条件
                Aggregation.match(criteria),
                //分组条件
                Aggregation.group("workDate")
                        .first("workDate")
                        .as("workDate")
                        //3、统计号源数量
                        .count().as("docCount")
                        //可预约数求和
                        .sum("reservedNumber").as("reservedNumber")
                        //剩余数量求和
                        .sum("availableNumber").as("availableNumber"),
                //排序
                Aggregation.sort(Sort.Direction.DESC, "workDate"),
                //4、分页操作
                Aggregation.skip((current - 1) * limit),
                Aggregation.limit(limit)
        );
        AggregationResults<BookingScheduleRuleVo> aggResults =
                mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggResults.getMappedResults();

        //查询分组的总记录数
        Aggregation totalagg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalaggregate = mongoTemplate.aggregate(totalagg, Schedule.class, BookingScheduleRuleVo.class);
        //总记录数
        int total = totalaggregate.getMappedResults().size();
        //把获取的日期转换成对应周
        for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
            String dayOfWeek = this.getDayOfWeek(new DateTime(bookingScheduleRuleVo.getWorkDate()));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("bookingScheduleRuleVoList", bookingScheduleRuleVoList);
        result.put("total", total);
        //根据医院编号查询医院名称
        String hosName = hospitalService.selectByhospcode(hoscode);
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosName", hosName);
        result.put("baseMap", baseMap);
        return result;
    }

    public List<Schedule> getScheduleDeatil(String hoscode, String depcode, String workDate) {


        DateTime dateTime = new DateTime(workDate);
        System.out.println(dateTime.toDate());
        List<Schedule> scheduleList = scheduleRepository.getScheduleByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, new DateTime(workDate));

        scheduleList.stream().forEach(item -> {

            //封装返回的信息，为返回类添加医院名称，科室名称，日期
            this.packageSchedule(item);

        });

        return scheduleList;
    }

    //获取可预约的排班数据
    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        Map<String, Object> result = new HashMap<>();
        //根据医院编号查询医院
        //先查询医院表
        Hospital hospital = hospitalService.getByHocode(hoscode);
        if (hospital == null) {
            throw new YyghException(ResultCodeEnum.DATA_UPDATE_ERROR);
        }
        //获取医院预约规则
        BookingRule bookingRule = hospital.getBookingRule();
        //分页查询可预约日期
        IPage iPage = this.getListDate(page, limit, bookingRule);
        List<Date> dateList = iPage.getRecords();
        //获取可预约日期科室剩余预约数
        Criteria criteria = Criteria.where("hoscode").is(hoscode).
                and("depcode").is(depcode).and("workDate").in(dateList);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("workDate")
                        .first("workDate").as("workDate")
                        .count().as("doCount")
                        .sum("availableNumber").as("availableNumber")
                        .sum("reservedNumber").as("reservedNumber")
        );
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleVoList = aggregate.getMappedResults();
        //合并数据 ，map的key为日期 ，value值为 预约规则和剩余数量等
        //合并数据 将统计数据ScheduleVo根据“安排日期”合并到BookingRuleVo
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(scheduleVoList)) {
            scheduleVoMap = scheduleVoList.stream()
                    .collect
                            (Collectors.toMap(BookingScheduleRuleVo::getWorkDate,
                                    BookingScheduleRuleVo -> BookingScheduleRuleVo));
        }
        //获取可预约的排班规则
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        for (int i = 0, len = dateList.size(); i < len; i++) {
            Date date = dateList.get(i);
            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
            if (null == bookingScheduleRuleVo) { // 说明当天没有排班医生
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //就诊医生人数
                bookingScheduleRuleVo.setDocCount(0);
                //科室剩余预约数  -1表示无号
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            //计算当前预约日期为周几
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

            //最后一页最后一条记录为即将预约   状态 0：正常 1：即将放号 -1：当天已停止挂号
            if (i == len - 1 && page == iPage.getPages()) {
                bookingScheduleRuleVo.setStatus(1);
            } else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //当天预约如果过了停号时间， 不能预约
            if (i == 0 && page == 1) {
                DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if (stopTime.isBeforeNow()) {
                    //停止预约
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }

        //可预约日期规则数据
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", iPage.getTotal());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getByHocode(hoscode).getHosname());
        //科室
        Department department = departmentService.getDepartment(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
//月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
//放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
//停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;
    }

    @Override
    public List<Schedule> getShcedule(String hoscode, String depcode, String workDate) {
        List<Schedule> list = scheduleRepository.getScheduleByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, new DateTime(workDate));

        return list;
    }

    @Override
    public Schedule getById(String scheduleId) {

        Schedule schedule1 = scheduleRepository.findById(scheduleId).get();
        return this.packageSchedule(schedule1);
    }

    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo=new ScheduleOrderVo();
        Schedule schedule = baseMapper.selectById(scheduleId);
        if(schedule==null ){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //获取预约规则信息
        Hospital hospital = hospitalService.getByHocode(schedule.getHoscode());
        if(null == hospital) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        if(null == bookingRule) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospitalService.getByHocode(schedule.getHoscode()).getHosname());
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(departmentService.getDepartmentByHoscodeAndDepcode( schedule.getDepcode(),schedule.getHoscode()));
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());
        //退号截止天数（如：就诊前一天为-1，当天为0）
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //预约开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //预约截止时间
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStartTime(startTime.toDate());
        return scheduleOrderVo;

    }

    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        scheduleRepository.save(schedule);
    }

    private IPage getListDate(Integer page, Integer limit, BookingRule bookingRule) {
        //获取当天的放号时间   年月日 时分秒
        DateTime dateTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        //获取预约的周期
        Integer cycle = bookingRule.getCycle();
        //当天时间已过不能预约，预约周期从后一天开始计算 ，预约周期加一
        if (dateTime.isBeforeNow()) {
            cycle += 1;
        }
        //获取可预约所有日期 ，最后一天显示即将放号
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            DateTime curDateTime = dateTime.plusDays(i);
            String s = curDateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(s).toDate());
        }
        //因为最多显示七天，超过七天进行分页操作
        List<Date> pagedateList = new ArrayList<>();
        //2   7
        int start = (page - 1) * limit;
        //所有记录数
        int end = (page - 1) * limit + limit;
        //如果数据小于七 直接显示
        if (end > dateList.size()) {
            end = dateList.size();
        }
        for (int i = start; i < dateList.size(); i++) {
            pagedateList.add(dateList.get(i));
        }
        //如果显示的数据大于7进行分页
        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page(start, 7, dateList.size());
        iPage.setRecords(pagedateList);
        return iPage;
    }

    private DateTime getDateTime(Date date, String releaseTime) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " " + releaseTime;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }

    private Schedule packageSchedule(Schedule item) {
        //封装医院名称
        item.getParam().put("hosname", hospitalService.selectByhospcode(item.getHoscode()));
        //封装科室
        item.getParam().put("depname", departmentService.getDepartmentByHoscodeAndDepcode(item.getDepcode(), item.getHoscode()));

        item.getParam().put("dateOfWeek", this.getDayOfWeek(new DateTime(item.getWorkDate())));

        return item;
    }

    //自己写好的工具类
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }

}

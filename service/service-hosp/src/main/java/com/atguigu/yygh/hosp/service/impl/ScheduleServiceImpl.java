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
        //????????????????????????
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
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()//????????????
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);//?????????????????????????????????
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
        //1??????????????????????????????????????????????????????????????????
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        //2??????????????????workDate???????????????
        Aggregation agg = Aggregation.newAggregation(
                //????????????
                Aggregation.match(criteria),
                //????????????
                Aggregation.group("workDate")
                        .first("workDate")
                        .as("workDate")
                        //3?????????????????????
                        .count().as("docCount")
                        //??????????????????
                        .sum("reservedNumber").as("reservedNumber")
                        //??????????????????
                        .sum("availableNumber").as("availableNumber"),
                //??????
                Aggregation.sort(Sort.Direction.DESC, "workDate"),
                //4???????????????
                Aggregation.skip((current - 1) * limit),
                Aggregation.limit(limit)
        );
        AggregationResults<BookingScheduleRuleVo> aggResults =
                mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggResults.getMappedResults();

        //???????????????????????????
        Aggregation totalagg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalaggregate = mongoTemplate.aggregate(totalagg, Schedule.class, BookingScheduleRuleVo.class);
        //????????????
        int total = totalaggregate.getMappedResults().size();
        //????????????????????????????????????
        for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
            String dayOfWeek = this.getDayOfWeek(new DateTime(bookingScheduleRuleVo.getWorkDate()));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("bookingScheduleRuleVoList", bookingScheduleRuleVoList);
        result.put("total", total);
        //????????????????????????????????????
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

            //??????????????????????????????????????????????????????????????????????????????
            this.packageSchedule(item);

        });

        return scheduleList;
    }

    //??????????????????????????????
    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        Map<String, Object> result = new HashMap<>();
        //??????????????????????????????
        //??????????????????
        Hospital hospital = hospitalService.getByHocode(hoscode);
        if (hospital == null) {
            throw new YyghException(ResultCodeEnum.DATA_UPDATE_ERROR);
        }
        //????????????????????????
        BookingRule bookingRule = hospital.getBookingRule();
        //???????????????????????????
        IPage iPage = this.getListDate(page, limit, bookingRule);
        List<Date> dateList = iPage.getRecords();
        //??????????????????????????????????????????
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
        //???????????? ???map???key????????? ???value?????? ??????????????????????????????
        //???????????? ???????????????ScheduleVo?????????????????????????????????BookingRuleVo
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(scheduleVoList)) {
            scheduleVoMap = scheduleVoList.stream()
                    .collect
                            (Collectors.toMap(BookingScheduleRuleVo::getWorkDate,
                                    BookingScheduleRuleVo -> BookingScheduleRuleVo));
        }
        //??????????????????????????????
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        for (int i = 0, len = dateList.size(); i < len; i++) {
            Date date = dateList.get(i);
            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
            if (null == bookingScheduleRuleVo) { // ??????????????????????????????
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //??????????????????
                bookingScheduleRuleVo.setDocCount(0);
                //?????????????????????  -1????????????
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            //?????????????????????????????????
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

            //?????????????????????????????????????????????   ?????? 0????????? 1??????????????? -1????????????????????????
            if (i == len - 1 && page == iPage.getPages()) {
                bookingScheduleRuleVo.setStatus(1);
            } else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //??????????????????????????????????????? ????????????
            if (i == 0 && page == 1) {
                DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if (stopTime.isBeforeNow()) {
                    //????????????
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }

        //???????????????????????????
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", iPage.getTotal());
        //??????????????????
        Map<String, String> baseMap = new HashMap<>();
        //????????????
        baseMap.put("hosname", hospitalService.getByHocode(hoscode).getHosname());
        //??????
        Department department = departmentService.getDepartment(hoscode, depcode);
        //???????????????
        baseMap.put("bigname", department.getBigname());
        //????????????
        baseMap.put("depname", department.getDepname());
//???
        baseMap.put("workDateString", new DateTime().toString("yyyy???MM???"));
//????????????
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
//????????????
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
        //????????????????????????
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
        //?????????????????????????????????????????????-1????????????0???
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //??????????????????
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //??????????????????
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //????????????????????????
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
        //???????????????????????????   ????????? ?????????
        DateTime dateTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        //?????????????????????
        Integer cycle = bookingRule.getCycle();
        //????????????????????????????????????????????????????????????????????? ?????????????????????
        if (dateTime.isBeforeNow()) {
            cycle += 1;
        }
        //??????????????????????????? ?????????????????????????????????
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            DateTime curDateTime = dateTime.plusDays(i);
            String s = curDateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(s).toDate());
        }
        //?????????????????????????????????????????????????????????
        List<Date> pagedateList = new ArrayList<>();
        //2   7
        int start = (page - 1) * limit;
        //???????????????
        int end = (page - 1) * limit + limit;
        //????????????????????? ????????????
        if (end > dateList.size()) {
            end = dateList.size();
        }
        for (int i = start; i < dateList.size(); i++) {
            pagedateList.add(dateList.get(i));
        }
        //???????????????????????????7????????????
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
        //??????????????????
        item.getParam().put("hosname", hospitalService.selectByhospcode(item.getHoscode()));
        //????????????
        item.getParam().put("depname", departmentService.getDepartmentByHoscodeAndDepcode(item.getDepcode(), item.getHoscode()));

        item.getParam().put("dateOfWeek", this.getDayOfWeek(new DateTime(item.getWorkDate())));

        return item;
    }

    //????????????????????????
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "??????";
            default:
                break;
        }
        return dayOfWeek;
    }

}

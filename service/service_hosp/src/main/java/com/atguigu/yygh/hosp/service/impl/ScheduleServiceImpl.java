package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;

/*====================================================
                时间: 2022-05-28
                讲师: 刘  辉
                出品: 尚硅谷教学团队
======================================================*/
@Service
public class ScheduleServiceImpl  implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private DepartmentService  departmentService;

    @Override
    public void saveSchedule(Map<String, Object> stringObjectMap) {
        Schedule schedule = JSONObject.parseObject(JSONObject.toJSONString(stringObjectMap), Schedule.class);
        String hoscode = schedule.getHoscode();
        String depcode = schedule.getDepcode();
        String hosScheduleId = schedule.getHosScheduleId();

//        Schedule platformSchedule=scheduleRepository.findByHoscodeAndDepcodeAndHosScheduleId(hoscode,depcode,hosScheduleId);
//
//        if(platformSchedule == null){
//            schedule.setCreateTime(new Date());
//            schedule.setUpdateTime(new Date());
//            schedule.setIsDeleted(0);
//            scheduleRepository.save(schedule);
//        }else{
//            schedule.setCreateTime(platformSchedule.getCreateTime());
//            schedule.setUpdateTime(new Date());
//            schedule.setIsDeleted(platformSchedule.getIsDeleted());
//            schedule.setId(platformSchedule.getId());
//            scheduleRepository.save(schedule);
//        }
        Query query = new Query(Criteria.where("hoscode").is(hoscode)
                .and("depcode").is(depcode)
                .and("hosScheduleId").is(hosScheduleId));

        Update update = new Update();
        update.set("updateTime", new Date());
        stringObjectMap.forEach((key, value) -> {
            if (!"hoscode".equals(key) && !"depcode".equals(key) && !"hosScheduleId".equals(key)) {
                update.set(key, value);
            }
        });
        update.setOnInsert("createTime", new Date());
        update.setOnInsert("isDeleted", 0);

        mongoTemplate.upsert(query, update, Schedule.class);

    }

    @Override
    public Page<Schedule> getSchedulePage(Map<String, Object> stringObjectMap) {
        Schedule schedule=new Schedule();
        String hoscode = (String)stringObjectMap.get("hoscode");
        schedule.setHoscode(hoscode);
        Example<Schedule> scheduleExample=Example.of(schedule);

        int page = Integer.parseInt(stringObjectMap.get("page").toString());
        int limit = Integer.parseInt(stringObjectMap.get("limit").toString());


        PageRequest pageRequest = PageRequest.of(page-1, limit, Sort.by("createTime").ascending());

        Page<Schedule> result = scheduleRepository.findAll(scheduleExample, pageRequest);
        return result;
    }

    @Override
    public void remove(Map<String, Object> stringObjectMap) {
       String hoscode =  (String)stringObjectMap.get("hoscode");
       String hosScheduleId =  (String)stringObjectMap.get("hosScheduleId");

       Schedule schedule= scheduleRepository.findByHoscodeAndHosScheduleId(hoscode,hosScheduleId);

       if(schedule != null){
           scheduleRepository.deleteById(schedule.getId());
       }

    }

    @Override
    public Map<String, Object> page(Integer pageNum, Integer pageSize, String hoscode, String depcode) {
        //1 根据医院编号 和 科室编号 查询
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        //2 根据工作日workDate期进行分组
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),//匹配条件
                Aggregation.group("workDate")//分组字段
                        .first("workDate").as("workDate")
                        //3 统计号源数量
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                //排序
                Aggregation.sort(Sort.Direction.ASC,"workDate"),
                //4 实现分页
                Aggregation.skip((pageNum-1)*pageSize),
                Aggregation.limit(pageSize)
        );
        //调用方法，最终执行
        AggregationResults<BookingScheduleRuleVo> aggResults =
                mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggResults.getMappedResults();
        //分组查询的总记录数
        Aggregation totalAgg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalAggResults =
                mongoTemplate.aggregate(totalAgg,
                        Schedule.class, BookingScheduleRuleVo.class);
        int total = totalAggResults.getMappedResults().size();
        //把日期对应星期获取
        for(BookingScheduleRuleVo bookingScheduleRuleVo:bookingScheduleRuleVoList) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }
        //设置最终数据，进行返回
        Map<String, Object> result = new HashMap<>();
        result.put("bookingScheduleRuleList",bookingScheduleRuleVoList);
        result.put("total",total);
        //获取医院名称
        Hospital hospital = hospitalService.getHospitalByHoscode(hoscode);
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname",hospital.getHosname());
        result.put("baseMap",baseMap);
        return result;

    }

    @Override
    public List<Schedule> detail(String hoscode, String depcode, String workDate) {
        // 转换字符串日期为Date对象
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(workDate);
        } catch (java.text.ParseException e) {
            throw new RuntimeException("日期格式错误: " + workDate + ", 期望格式: yyyy-MM-dd", e);
        }

        // 使用 repository 方法查询
        List<Schedule> scheduleList = scheduleRepository.findByHoscodeAndDepcodeAndWorkDateEquals(hoscode, depcode, date);
        scheduleList.forEach(this::packageSchedule);
        return scheduleList;


    }

    /**
     * 根据日期获取周几数据
     * @param dateTime
     * @return
     */
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
    //封装排班详情其他值 医院名称、科室名称、日期对应星期
    private void packageSchedule(Schedule schedule) {
        //设置医院名称
        schedule.getParam().put("hosname",hospitalService.getHospitalByHoscode(schedule.getHoscode()).getHosname());
        //设置科室名称
        schedule.getParam().put("depname",
                departmentService.getDepName(schedule.getHoscode(),schedule.getDepcode()).getDepname());
        //设置日期对应星期
        schedule.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
    }
}

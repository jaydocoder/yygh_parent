package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/*====================================================
                时间: 2022-05-28
                讲师: 刘  辉
                出品: 尚硅谷教学团队
======================================================*/
@Service
public class ScheduleServiceImpl  implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;
    @Override
    public void saveSchedule(Map<String, Object> stringObjectMap) {
        Schedule schedule = JSONObject.parseObject(JSONObject.toJSONString(stringObjectMap), Schedule.class);
        String hoscode = schedule.getHoscode();
        String depcode = schedule.getDepcode();
        String hosScheduleId = schedule.getHosScheduleId();

        Schedule platformSchedule=scheduleRepository.findByHoscodeAndDepcodeAndHosScheduleId(hoscode,depcode,hosScheduleId);

        if(platformSchedule == null){
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);
        }else{
            schedule.setCreateTime(platformSchedule.getCreateTime());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(platformSchedule.getIsDeleted());
            schedule.setId(platformSchedule.getId());
            scheduleRepository.save(schedule);
        }

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
}

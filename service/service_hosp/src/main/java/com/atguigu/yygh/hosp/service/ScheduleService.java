package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Schedule;
import org.springframework.data.domain.Page;

import java.util.Map;

/*************************************************
 时间: 2022-05-28
 讲师: 刘  辉
 出品: 尚硅谷教学团队
 **************************************************/
public interface ScheduleService {
    void saveSchedule(Map<String, Object> stringObjectMap);

    Page<Schedule> getSchedulePage(Map<String, Object> stringObjectMap);

    void remove(Map<String, Object> stringObjectMap);
}

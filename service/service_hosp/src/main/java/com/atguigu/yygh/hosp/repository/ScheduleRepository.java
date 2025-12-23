package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;

/*************************************************
 时间: 2022-05-28
 讲师: 刘  辉
 出品: 尚硅谷教学团队
 **************************************************/
public interface ScheduleRepository  extends MongoRepository<Schedule,String> {
    Schedule findByHoscodeAndDepcodeAndHosScheduleId(String hoscode, String depcode, String hosScheduleId);

    Schedule findByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);
}

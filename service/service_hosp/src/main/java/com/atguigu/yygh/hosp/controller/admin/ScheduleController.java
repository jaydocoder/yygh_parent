package com.atguigu.yygh.hosp.controller.admin;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * ClassName: ScheduleController
 * Package: com.atguigu.yygh.hosp.controller.admin
 * Description:
 *
 * @Author yijie
 * @Create 2026/1/10  20:32
 * @Version 1.0
 */
@RestController
@RequestMapping("/admin/hosp/schedule")
public class ScheduleController {
    @Autowired
    private ScheduleService scheduleService;
    @GetMapping("/{pageSize}/{pageNum}/{hoscode}/{depcode}")
    public R getScheduleList(@PathVariable Integer pageSize,
                             @PathVariable Integer pageNum,
                             @PathVariable String hoscode,
                             @PathVariable String depcode){
        Map<String, Object> map = scheduleService.page(pageNum,pageSize,hoscode,depcode);
        return R.ok().data(map);

    }
    @GetMapping("/{hoscode}/{depcode}/{workDate}")
    public R getScheduleRule(@PathVariable String hoscode,
                             @PathVariable String depcode,
                             @PathVariable String workDate){
        List<Schedule> scheduleList = scheduleService.detail(hoscode, depcode, workDate);
        return R.ok().data("list",scheduleList);
    }
}

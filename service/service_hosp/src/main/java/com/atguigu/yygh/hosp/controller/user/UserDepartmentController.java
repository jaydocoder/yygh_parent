package com.atguigu.yygh.hosp.controller.user;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ClassName: UserDepartmentController
 * Package: com.atguigu.yygh.hosp.controller.user
 * Description:
 *
 * @Author yijie
 * @Create 2026/1/19  11:40
 * @Version 1.0
 */
@RestController
@RequestMapping("/user/hosp/department")
public class UserDepartmentController {
    @Autowired
    private DepartmentService departmentService;

    @RequestMapping("/all/{hoscode}")
    public R findAll(@PathVariable String hoscode){
        List<DepartmentVo> departmentVos = departmentService.getDepartmentList(hoscode);
        return R.ok().data("list",departmentVos);

    }
}

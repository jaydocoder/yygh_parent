package com.atguigu.yygh.hosp.controller.admin;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ClassName: DepartentController
 * Package: com.atguigu.yygh.hosp.controller.admin
 * Description:
 *
 * @Author yijie
 * @Create 2026/1/10  14:47
 * @Version 1.0
 */
@RestController
@RequestMapping("/admin/hosp/department")
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;
    @GetMapping("/{hoscode}")
    public R getDepartmentList(@PathVariable String hoscode){
    List<DepartmentVo> list = departmentService.getDepartmentList(hoscode);
    return R.ok().data("list",list);

    }
}

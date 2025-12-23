package com.atguigu.yygh.hosp.controller.admin;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/*====================================================
                时间: 2022-05-28
                讲师: 刘  辉
                出品: 尚硅谷教学团队
======================================================*/
@RestController
@RequestMapping("/admin/hospital")
@CrossOrigin
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;


    @GetMapping("/{pageNum}/{pageSize}")
    public R getHospitalPage(@PathVariable Integer pageNum,@PathVariable Integer pageSize, HospitalQueryVo hospitalQueryVo){

        Page<Hospital> hospitalPage= hospitalService.getHospitalPage(pageNum,pageSize,hospitalQueryVo);

        return R.ok().data("total",hospitalPage.getTotalElements()).data("list",hospitalPage.getContent());
    }
}

package com.atguigu.yygh.hosp.controller.user;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ClassName: UserHospitalController
 * Package: com.atguigu.yygh.hosp.controller.user
 * Description:
 *
 * @Author yijie
 * @Create 2026/1/18  15:49
 * @Version 1.0
 */
@RestController
@RequestMapping("/user/hosp/hospital")
public class UserHospitalController {
    @Autowired
    private HospitalService hospitalService;
    @GetMapping("/list")
    public R getHopitalList(HospitalQueryVo hospitalQueryVo) {
        Page<Hospital> page = hospitalService.getHospitalPage(1, 10000000, hospitalQueryVo);
        return R.ok().data("list",page.getContent());
    }
    //根据医院名字进行模糊查询
    @GetMapping("/{hosname}")
    public R findByName(@PathVariable String hosname) {
        List<Hospital> hospital = hospitalService.findByName(hosname);
        return R.ok().data("list",hospital);
    }
    @GetMapping("/detail/{hoscode}")
    public R getHospitalDetail(@PathVariable String hoscode) {
        Hospital hospital = hospitalService.getHospitalDetail(hoscode);
        return R.ok().data("hospital",hospital);
    }
}

package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.client.DictFeignClient;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.hosp.mapper.HospitalSetMapper;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/*====================================================
                时间: 2022-05-27
                讲师: 刘  辉
                出品: 尚硅谷教学团队
======================================================*/
@Service
public class HospitalServiceImpl  implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;
    @Autowired
    private HospitalSetMapper hospitalSetMapper;

    @Autowired
    private DictFeignClient dictFeignClient;
    @Autowired
    private MongoTemplate  mongoTemplate;


    @Override
    public void saveHospital(Map<String, Object> resultMap) {
        Hospital hospital = JSONObject.parseObject(JSONObject.toJSONString(resultMap), Hospital.class);

        String hoscode = hospital.getHoscode();
        Hospital collection=hospitalRepository.findByHoscode(hoscode);

        if(collection == null){//平台上没有该医院信息做添加
            //0
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }else{//平台上有该医院信息做修改
            hospital.setStatus(collection.getStatus());
            hospital.setCreateTime(collection.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(collection.getIsDeleted());


            hospital.setId(collection.getId());
            hospitalRepository.save(hospital);
        }


    }

    @Override
    public String getSignKeyWithHoscode(String requestHoscode) {
        QueryWrapper<HospitalSet> hospitalSetQueryWrapper=new QueryWrapper<HospitalSet>();
        hospitalSetQueryWrapper.eq("hoscode", requestHoscode);
        HospitalSet hospitalSet = hospitalSetMapper.selectOne(hospitalSetQueryWrapper);
        if(hospitalSet == null){
           throw new YyghException(20001,"该医院信息不存在");
        }
        return hospitalSet.getSignKey();
    }

    @Override
    public Hospital getHospitalByHoscode(String hoscode) {
        return  hospitalRepository.findByHoscode(hoscode);
    }

    @Override
    public Page<Hospital> getHospitalPage(Integer pageNum, Integer pageSize, HospitalQueryVo hospitalQueryVo) {

        Hospital hospital=new Hospital();
        if(!StringUtils.isEmpty(hospitalQueryVo.getHosname())){
            hospital.setHosname(hospitalQueryVo.getHosname());
        }
        if(!StringUtils.isEmpty(hospitalQueryVo.getProvinceCode())){
            hospital.setProvinceCode(hospitalQueryVo.getProvinceCode());
        }
        if(!StringUtils.isEmpty(hospitalQueryVo.getCityCode())){
            hospital.setCityCode(hospitalQueryVo.getCityCode());
        }
        if(!StringUtils.isEmpty(hospitalQueryVo.getHostype())){
            hospital.setHostype(hospitalQueryVo.getHostype());
        }
        if(!StringUtils.isEmpty(hospitalQueryVo.getDistrictCode())){
            hospital.setDistrictCode(hospitalQueryVo.getDistrictCode());
        }

        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                //.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withMatcher("hosname", ExampleMatcher.GenericPropertyMatchers.contains())
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写

        Example<Hospital> of = Example.of(hospital,matcher);


        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, Sort.by("createTime").ascending());

        Page<Hospital> page = hospitalRepository.findAll(of, pageRequest);


        page.getContent().stream().forEach(item->{
           this.packageHospital(item);
        });

        return page;
    }

    @Override
    public void updateById(String id, Hospital hospital) {
        //先根据id 查询现有文档
        Hospital existingHospital = hospitalRepository.findById(id).orElse( null);

        if(existingHospital != null){
            existingHospital.setStatus(hospital.getStatus());
            existingHospital.setUpdateTime(new Date());
            hospitalRepository.save(existingHospital);
        }
        // 构建查询条件：根据MongoDB主键_id
//        Query query = new Query(Criteria.where("_id").is(id));
//        Update update = new Update()
//                .set("status", hospital.getStatus())
//                .set("updateTime", new Date());
//        mongoTemplate.updateFirst(query, update, Hospital.class);

    }

    @Override
    public Hospital getHospitalById(String id) {
        //根据id查询mongo DB里面的数据
        Hospital hospital = hospitalRepository.findById(id).orElse(null);
        if(hospital != null){
            this.packageHospital(hospital);
            return hospital;
        }
        return null;
    }

    @Override
    public List<Hospital> findByName(String hosname) {
        // 参数校验
        if (!StringUtils.hasText(hosname)) {
            return new ArrayList<>(); // 返回空集合而不是null
        }
        List<Hospital> hospitals = hospitalRepository.findByHosnameContainingIgnoreCase(hosname.trim());
        // 处理查询结果
        if (hospitals != null && !hospitals.isEmpty()) {
            hospitals.forEach(this::packageHospital);
        }

        return hospitals;

    }

    @Override
    public Hospital getHospitalDetail(String hoscode) {
        Hospital hospital = hospitalRepository.findByHoscode(hoscode);
        if (hospital != null) {
            this.packageHospital(hospital);
        }
        return hospital;

    }

    private void packageHospital(Hospital hospital){
        String hostype = hospital.getHostype();

        String provinceCode = hospital.getProvinceCode();
        String cityCode = hospital.getCityCode();
        String districtCode = hospital.getDistrictCode();


        String provinceAddress = dictFeignClient.getNameByValue(Long.parseLong(provinceCode));
        String cityAddress = dictFeignClient.getNameByValue(Long.parseLong(cityCode));
        String districtAddress = dictFeignClient.getNameByValue(Long.parseLong(districtCode));

        String level = dictFeignClient.getNameByDictCodeAndValue(DictEnum.HOSTYPE.getDictCode(), Long.parseLong(hostype));

        hospital.getParam().put("hostypeString", level);
        hospital.getParam().put("fullAddress", provinceAddress+cityAddress+districtAddress + hospital.getAddress());

    }
}

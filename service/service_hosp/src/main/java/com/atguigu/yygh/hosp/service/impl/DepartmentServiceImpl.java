package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/*====================================================
                时间: 2022-05-27
                讲师: 刘  辉
                出品: 尚硅谷教学团队
======================================================*/
@Service
public class DepartmentServiceImpl  implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void saveDepartment(Map<String, Object> stringObjectMap) {

        Department department = JSONObject.parseObject(JSONObject.toJSONString(stringObjectMap), Department.class);
        //医院编号+科室编号 联合查询
        String hoscode = department.getHoscode();
        String depcode = department.getDepcode();

        Department platformDepartment = departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);

        if (platformDepartment == null) { //如果mongo中没有该科室信息，做添加操作
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        } else { //如果mongo中有该科室信息，做修改操作

            department.setCreateTime(platformDepartment.getCreateTime());
            department.setUpdateTime(new Date());
            department.setIsDeleted(platformDepartment.getIsDeleted());
            department.setId(platformDepartment.getId());
            departmentRepository.save(department);
        }

        //departmentRepository.save
    }

    @Override
    public Page<Department> getDepartmentPage(Map<String, Object> stringObjectMap) {
        Integer page = Integer.parseInt((String) stringObjectMap.get("page"));
        Integer limit = Integer.parseInt((String) stringObjectMap.get("limit"));

        String hoscode = (String) stringObjectMap.get("hoscode");

        Department department = new Department();
        department.setHoscode(hoscode);

        Example<Department> example = Example.of(department);

        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Department> all = departmentRepository.findAll(example, pageable);
        return all;
    }

    @Override
    public void remove(Map<String, Object> stringObjectMap) {
        String hoscode = (String) stringObjectMap.get("hoscode");
        String depcode = (String) stringObjectMap.get("depcode");

        departmentRepository.deleteByHoscodeAndDepcode(hoscode, depcode);

    }

    @Override
    public List<DepartmentVo> getDepartmentList(String hoscode) {
        Department department = new Department();
        department.setHoscode(hoscode);
        Example<Department> example = Example.of(department);
        List<Department> all = departmentRepository.findAll(example);

        //map的key:就是当前科室所属大客科室的编号
        //map的Value:就是当前大科室地下的所有子科室信息。
        List<DepartmentVo> bigDepartmentList = new ArrayList<>();
        Map<String, List<Department>> collect = all.stream().collect(Collectors.groupingBy(Department::getBigcode));
        for (Map.Entry<String, List<Department>> entry : collect.entrySet()) {
            DepartmentVo bigDepartmentVo = new DepartmentVo();
            //大科室编号
            String bigcode = entry.getKey();
            //当前大科室下面的子列表
            List<Department> bigDeptList = entry.getValue();
            List<DepartmentVo> childDepartmentVoList = new ArrayList<>();
            for (Department childDepartment : bigDeptList) {
                DepartmentVo childDepartmentVo = new DepartmentVo();
                //当前子科室的编号
                String depcode = childDepartment.getDepcode();
                //当前子科室的名称
                String depname = childDepartment.getDepname();
                childDepartmentVo.setDepcode(depcode);
                childDepartmentVo.setDepname(depname);
                childDepartmentVoList.add(childDepartmentVo);
            }

            bigDepartmentVo.setDepcode(bigcode);
            bigDepartmentVo.setDepname(bigDeptList.get(0).getBigname());
            bigDepartmentVo.setChildren(childDepartmentVoList);
            bigDepartmentList.add(bigDepartmentVo);
        }
        return bigDepartmentList;

    }

    @Override
    public Department getDepName(String hoscode, String depcode) {
        Department department = departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);
        if(department != null){
            return department;
        }
        return new Department();
    }



}

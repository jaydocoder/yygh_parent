package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Hospital;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/*************************************************
 时间: 2022-05-27
 讲师: 刘  辉
 出品: 尚硅谷教学团队
 **************************************************/
public interface HospitalRepository  extends MongoRepository<Hospital,String> {
    Hospital findByHoscode(String hoscode);

    List<Hospital> findByHosnameContainingIgnoreCase(String trim);
}

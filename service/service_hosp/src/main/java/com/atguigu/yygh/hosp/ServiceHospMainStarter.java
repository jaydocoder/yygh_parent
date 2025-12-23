package com.atguigu.yygh.hosp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/*====================================================
                时间: 2022-05-18
                讲师: 刘  辉
                出品: 尚硅谷教学团队
======================================================*/
@SpringBootApplication
@ComponentScan(value = "com.atguigu.yygh")
@MapperScan(value = "com.atguigu.yygh.hosp.mapper")
@EnableDiscoveryClient //@EnableEurekaClient
@EnableFeignClients(basePackages = "com.atguigu.yygh")
public class ServiceHospMainStarter {

    public static void main(String[] args) {
        SpringApplication.run(ServiceHospMainStarter.class,args);
    }
}

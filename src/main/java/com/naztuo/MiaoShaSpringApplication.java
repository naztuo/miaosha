package com.naztuo;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@MapperScan(basePackages = {"com.naztuo.user.dao","com.naztuo.miaosha.dao"})
public class MiaoShaSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiaoShaSpringApplication.class);
    }
}

package com.community.cfgs;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.community.cfgs.mapper")
public class CfgsApplication {
    public static void main(String[] args) {
        SpringApplication.run(CfgsApplication.class, args);
        System.out.println(" 运行成功！！！ ");
    }
}

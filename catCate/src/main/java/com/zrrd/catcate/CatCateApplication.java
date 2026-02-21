package com.zrrd.catcate;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//扫描mapper
@MapperScan("com.zrrd.catcate.mapper")
public class CatCateApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatCateApplication.class, args);
    }

}

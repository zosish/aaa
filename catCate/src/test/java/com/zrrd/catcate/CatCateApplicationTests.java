package com.zrrd.catcate;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SpringBootTest
class CatCateApplicationTests {

    @Test
    void contextLoads() {
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/catcate", "root", "18697098685")
                .globalConfig(builder -> {
                    builder.author("xyd") // 设置作者
//							.enableSwagger() // 开启 swagger 模式
                            .outputDir("D:\\javaMaven\\catCate\\src\\main\\java"); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("com.zrrd") // 设置父包名
                            .moduleName("catcate") // 设置父包模块名
                            .pathInfo(Collections.singletonMap(OutputFile.xml, "D:\\javaMaven\\catCate\\src\\main\\resources\\mapper")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude("refunds") // 设置需要生成的表名
//                    builder.addInclude(getTables("all")) //生成全部表
                            .addTablePrefix("t_", "c_"); // 设置过滤表前缀
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }

    protected static List<String> getTables(String tables) {
        return "all".equals(tables) ? Collections.emptyList() : Arrays.asList(tables.split(","));
    }


}

package com.atguigu.yygh.hosp.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author nicc
 * @version 1.0
 * @className HospConfig
 * @description TODO
 * @date 2022-08-24 13:33
 */
@Configuration
@EnableTransactionManagement
@MapperScan("com.atguigu.yygh.hosp.mapper")
@ComponentScan(basePackages = {"com.atguigu"})
public class HospConfig {

    /**
     * 分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

}

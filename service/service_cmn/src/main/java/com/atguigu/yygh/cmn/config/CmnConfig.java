package com.atguigu.yygh.cmn.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author nicc
 * @version 1.0
 * @className CmnConfig
 * @description TODO
 * @date 2022-08-29 14:57
 */

@Configuration
@EnableTransactionManagement
@MapperScan("com.atguigu.yygh.cmn.mapper")
@ComponentScan(basePackages = {"com.atguigu"})
public class CmnConfig {

    /**
     * 分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

}

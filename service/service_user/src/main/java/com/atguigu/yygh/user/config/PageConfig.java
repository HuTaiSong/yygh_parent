package com.atguigu.yygh.user.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author nicc
 * @version 1.0
 * @className PageConfig
 * @description TODO
 * @date 2022-09-09 09:08
 */
@Configuration
@EnableTransactionManagement
public class PageConfig {

    /**
     * 分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }
}

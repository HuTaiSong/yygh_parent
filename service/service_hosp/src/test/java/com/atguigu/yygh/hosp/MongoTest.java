package com.atguigu.yygh.hosp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Date;

/**
 * @author nicc
 * @version 1.0
 * @className MongoTest
 * @description TODO
 * @date 2022-09-02 00:44
 */
@SpringBootTest
public class MongoTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void test(){
        System.out.println("今天：" + new Date());
    }

    public static void main(String[] args) {
        System.out.println("啦啦啦");
    }

}

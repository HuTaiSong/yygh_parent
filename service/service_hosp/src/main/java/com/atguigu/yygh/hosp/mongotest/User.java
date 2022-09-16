package com.atguigu.yygh.hosp.mongotest;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author nicc
 * @version 1.0
 * @className User
 * @description TODO
 * @date 2022-09-02 00:41
 */
@Document("User")  //User类 ==> 集合User
@Data
public class User {

    @Id
    private String id;

    private String name;
    private Integer age;
    private String email;
    private Date createDate;

}

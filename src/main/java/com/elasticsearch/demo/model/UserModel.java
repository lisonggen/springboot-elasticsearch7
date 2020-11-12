package com.elasticsearch.demo.model;

import lombok.Data;

import java.util.Date;

/**
 * @program: elasticsearch-demo
 * @description:
 * @author: Reagan Li
 * @create: 2020-11-09 22:21
 **/

@Data
public class UserModel {

    private Long userId;

    private String userName;

    private String phone;

    private String address;

    private Date birthday;

    private String email;

    private String idCard;

    private Date createTime;

    private Date updateTime;
}

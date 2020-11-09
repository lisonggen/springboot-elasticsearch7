package com.elasticsearch.demo.model;

import lombok.Data;

import java.util.Date;

/**
 * @program: elasticsearch-demo
 * @description:
 * @author: Reagan Li
 * @create: 2020-11-03 22:02
 **/
@Data
public class BookModel {

    private Long id;

    private String title;

    private String auth;

    private Integer wordCount;

    private Date publishDate;

    private String esId;

}

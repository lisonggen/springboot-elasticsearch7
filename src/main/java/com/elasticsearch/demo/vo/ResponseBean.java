package com.elasticsearch.demo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @program: elasticsearch-demo
 * @description:
 * @author: Reagan Li
 * @create: 2020-11-08 11:51
 **/

@Data
@AllArgsConstructor
public class ResponseBean {

    private Integer code;

    private String message;

    private Object data;
}

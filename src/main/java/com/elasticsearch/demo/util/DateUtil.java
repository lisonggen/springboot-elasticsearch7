package com.elasticsearch.demo.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @program: elasticsearch-demo
 * @description:
 * @author: Reagan Li
 * @create: 2020-11-04 09:06
 **/
public class DateUtil {

    public static String dateFormate(Date date, String formate) {
        SimpleDateFormat sdf = new SimpleDateFormat(formate);
        return sdf.format(date);
    }

    public static void main(String[] args) {
        System.out.println(dateFormate(new Date(), "yyyy-MM-dd hh:mm:ss"));
    }
}

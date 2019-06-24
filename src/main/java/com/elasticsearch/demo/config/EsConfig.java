package com.elasticsearch.demo.config;

import com.google.gson.Gson;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: elasticsearch-demo
 * @description: EsConfig
 * @author: Reagan Li
 * @create: 2019-06-24 15:11
 **/
@Configuration
public class EsConfig {

    @Bean
    public Gson gson(){
        return new Gson();
    }

    @Bean
    public RestHighLevelClient client(){
        RestHighLevelClient client=new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost",9200,"http")
                        //new HttpHost("localhost",9200,"http")
                        //如果有其他节点，在这里添加即可
                )
        );
        return client;
    }

}

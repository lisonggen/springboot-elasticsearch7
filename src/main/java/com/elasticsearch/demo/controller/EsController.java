package com.elasticsearch.demo.controller;

import com.google.gson.Gson;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

/**
 * @program: elasticsearch-demo
 * @description: es controller
 * @author: Reagan Li
 * @create: 2019-06-24 15:14
 **/
@RestController
public class EsController {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private Gson gson;

    @GetMapping("/book/getById")
    public ResponseEntity get(@RequestParam(name = "id") String id) {
        if (id.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        GetRequest getRequest = new GetRequest("book", "_doc", id);
        GetResponse response = null;
        try {
            response = client.get(getRequest, RequestOptions.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response.isExists()) {
            return new ResponseEntity(response.getSource(), HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/book/add")
    public ResponseEntity add(@RequestParam("author")String author,
                                          @RequestParam("title")String title,
                                          @RequestParam("word_count")Integer wordCount,
                                          @RequestParam("publish_date")
                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                              Date publishDate
    ) {
        IndexRequest request = new IndexRequest("book");


        try {
            XContentBuilder builder = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("author", author)
                    .field("title", title)
                    .field("word_count", wordCount)
                    .field("publish_date", publishDate.getTime())
                    .endObject();

            request.opType("index").source(builder);
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
            return new ResponseEntity(response.getId(), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/book/deleteById")
    public ResponseEntity deleteById(@RequestParam(name = "id") String id) {
        DeleteRequest request = new DeleteRequest("book", id);
        DeleteResponse response = null;
        try {
            response= client.delete(request,RequestOptions.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity(response.getResult(), HttpStatus.OK);
    }

    @PutMapping("/book/update")
    public ResponseEntity update(@RequestParam(name = "id") String id,
                                 @RequestParam(name = "title", required = false) String title,
                                 @RequestParam(name = "author", required = false) String author) {
        UpdateRequest request = new UpdateRequest("book", id);
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder()
                    .startObject();
            if (!StringUtils.isEmpty(title)) {
                builder.field("title", title);
            }
            if (!StringUtils.isEmpty(author)) {
                builder.field("author", author);
            }
            builder.endObject();
            request.doc(builder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        UpdateResponse response = null;
        try {
            response = client.update(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity(response.getResult(), HttpStatus.OK);
    }

    @PostMapping("/book/query")
    public ResponseEntity query(@RequestParam(value = "author", required = false) String author,
                               @RequestParam(value = "title", required = false) String title,
                               @RequestParam(value = "gt_word_count", defaultValue = "0") int gtWordCount,
                               @RequestParam(value = "lt_word_count", required = false) Integer ltWordCount
    ) {
        SearchRequest request = new SearchRequest("book");
        //构造bool查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (!StringUtils.isEmpty(author)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("author", author));
        }

        if (!StringUtils.isEmpty(title)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", title));
        }

        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("word_count")
                .from(gtWordCount);
        if (ltWordCount != null && ltWordCount > 0) {
            rangeQueryBuilder.to(ltWordCount);
        }

        boolQueryBuilder.filter(rangeQueryBuilder);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0).size(10).query(boolQueryBuilder);
        System.out.println(searchSourceBuilder);

        request.searchType(SearchType.DEFAULT).source(searchSourceBuilder);

        List<Map<String, Object>> list = new ArrayList<>();
        try {
            SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
            for (SearchHit s : client.search(request, RequestOptions.DEFAULT).getHits().getHits()) {
                list.add(s.getSourceAsMap());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity(list, HttpStatus.OK);
    }
}

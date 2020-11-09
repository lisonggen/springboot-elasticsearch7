package com.elasticsearch.demo.util;

import com.elasticsearch.demo.model.BookModel;
import com.elasticsearch.demo.vo.ResponseBean;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: elasticsearch-demo
 * @description:
 * @author: Reagan Li
 * @create: 2020-11-04 08:53
 **/

@Component
public class EsUtil {

    @Resource
    RestHighLevelClient restHighLevelClient;

    //创建索引，需要用到IK分词器
    public ResponseBean createIndex(String indexName) {
        try {
            // 配置信息
            Settings esSetting = Settings.builder()
                    .put("number_of_shards", 3)
                    .put("number_of_replicas", 1)
                    .build();

            XContentBuilder builder = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("properties")
                    .startObject()
                    .field("name").startObject().field("index", "true").field("type", "keyword").endObject()
                    .field("age").startObject().field("index", "true").field("type", "integer").endObject()
                    .field("money").startObject().field("index", "true").field("type", "double").endObject()
                    .field("address").startObject().field("index", "true").field("type", "text").field("analyzer", "ik_max_word").endObject()
                    .field("birthday").startObject().field("index", "true").field("type", "date").field("format", "strict_date_optional_time||epoch_millis").endObject()
                    .endObject()
                    .endObject();
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
            createIndexRequest.mapping(builder);
            createIndexRequest.settings(esSetting);
            CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            boolean acknowledged = createIndexResponse.isAcknowledged();
            if (acknowledged) {
                return new ResponseBean(200, "创建成功", null);
            } else {
                return new ResponseBean(1002, "创建失败", null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteIndex(String indexName) {
        boolean acknowledged = false;
        try {
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
            deleteIndexRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
            AcknowledgedResponse delete = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            acknowledged = delete.isAcknowledged();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return acknowledged;
    }

    public boolean isIndexExists(String indexName) {
        Boolean exists = false;
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        getIndexRequest.humanReadable(true);
        try {
            exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exists;
    }

    public ResponseEntity getBookById(String id) {
        if (id.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        GetRequest getRequest = new GetRequest("book", "_doc", id);
        GetResponse response = null;
        try {
            response = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response.isExists()) {
            return new ResponseEntity(response.getSource(), HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    public List<BookModel> queryBook(Long id,  String auth, String title, Integer gtWordCount,
                                           Integer ltWordCount, String fromPublishDate, String toPublishDate) {
        SearchRequest request = new SearchRequest("book");
        //构造bool查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (id != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("id", id));
        }

        if (!StringUtils.isEmpty(auth)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("auth", auth));
        }

        if (!StringUtils.isEmpty(title)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", title));
        }

        RangeQueryBuilder wordCountRangeQueryBuilder = QueryBuilders.rangeQuery("word_count")
                .from(gtWordCount);
        if (ltWordCount != null && ltWordCount > 0) {
            wordCountRangeQueryBuilder.to(ltWordCount);
        }
        boolQueryBuilder.filter(wordCountRangeQueryBuilder);

        RangeQueryBuilder publishDateRangeQueryBuilder = QueryBuilders.rangeQuery("publish_date");
        if (!StringUtils.isEmpty(fromPublishDate)) {
            publishDateRangeQueryBuilder.from(fromPublishDate);
        }
        if (!StringUtils.isEmpty(toPublishDate)) {
            publishDateRangeQueryBuilder.to(toPublishDate);
        }
        boolQueryBuilder.filter(publishDateRangeQueryBuilder);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0).size(100).query(boolQueryBuilder);
        System.out.println(searchSourceBuilder);

        request.searchType(SearchType.DEFAULT).source(searchSourceBuilder);

        List<BookModel> bookModelList = new ArrayList<>();
        try {
            for (SearchHit s : restHighLevelClient.search(request, RequestOptions.DEFAULT).getHits().getHits()) {
                BookModel bookModel = new BookModel();
                bookModel.setEsId(s.getId());
                if (s.getSourceAsMap().get("id") != null) {
                    bookModel.setId(Long.valueOf(s.getSourceAsMap().get("id").toString()));
                }
                bookModel.setAuth(s.getSourceAsMap().get("auth").toString());
                bookModel.setWordCount(Integer.valueOf(s.getSourceAsMap().get("word_count").toString()));
                bookModel.setTitle(s.getSourceAsMap().get("title").toString());
                bookModel.setPublishDate(new DateTime(s.getSourceAsMap().get("publish_date").toString()).toDate());
                bookModelList.add(bookModel);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bookModelList;
    }

    public ResponseEntity saveBook(BookModel bookModel) {
        IndexRequest request = new IndexRequest("book");
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("id", bookModel.getId().toString())
                    .field("auth", bookModel.getAuth())
                    .field("title", bookModel.getTitle())
                    .field("word_count", bookModel.getWordCount())
                    .field("publish_date", DateUtil.dateFormate(bookModel.getPublishDate(), "yyyy-MM-dd"))
                    .endObject();

            request.opType("index").source(builder);
            IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
            return new ResponseEntity(response.getId(), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity deleteBook(String esId) {
        DeleteByQueryRequest request = new DeleteByQueryRequest("book");
        request.setQuery(new TermQueryBuilder("id", esId));
        request.setSize(1);
        BulkByScrollResponse response = null;

        try {
            response= restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    public ResponseEntity update(BookModel bookModel) {
        //https://blog.csdn.net/john1337/article/details/103145901/
        UpdateRequest request = new UpdateRequest("book", bookModel.getEsId());
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
            if (bookModel.getId() != null) {
                builder.field("id", bookModel.getId());
            }
            if (!StringUtils.isEmpty(bookModel.getTitle())) {
                builder.field("title", bookModel.getTitle());
            }
            if (bookModel.getWordCount() != null) {
                builder.field("word_count", bookModel.getWordCount());
            }
            if (!StringUtils.isEmpty(bookModel.getAuth())) {
                builder.field("auth", bookModel.getAuth());
            }
            if (!StringUtils.isEmpty(bookModel.getPublishDate())) {
                builder.field("publish_date", DateUtil.dateFormate(bookModel.getPublishDate(), "yyyy-MM-dd"));
            }
            builder.endObject();
            request.doc(builder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            restHighLevelClient.update(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    public ResponseEntity updateByQuery(BookModel bookModel) {

        return new ResponseEntity(HttpStatus.OK);
    }
}

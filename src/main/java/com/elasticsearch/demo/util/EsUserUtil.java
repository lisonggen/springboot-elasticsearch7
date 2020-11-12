package com.elasticsearch.demo.util;

import com.elasticsearch.demo.model.UserModel;
import com.elasticsearch.demo.vo.ResponseBean;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
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
 * @create: 2020-11-09 22:44
 **/


@Slf4j
@Component
public class EsUserUtil {

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
                    .field("user_id").startObject().field("index", "true").field("type", "keyword").endObject()
                    .field("user_name").startObject().field("index", "true").field("type", "keyword").endObject()
                    .field("phone").startObject().field("index", "true").field("type", "keyword").endObject()
                    .field("address").startObject().field("index", "true").field("type", "text").field("analyzer", "ik_max_word").endObject()
                    .field("birthday").startObject().field("index", "true").field("type", "date").field("format", "strict_date_optional_time||epoch_millis").endObject()
                    .field("email").startObject().field("index", "true").field("type", "keyword").endObject()
                    .field("id_card").startObject().field("index", "true").field("type", "keyword").endObject()
                    .field("create_time").startObject().field("index", "true").field("type", "date").field("format", "strict_date_optional_time||epoch_millis").endObject()
                    .field("update_time").startObject().field("index", "true").field("type", "date").field("format", "strict_date_optional_time||epoch_millis").endObject()
                    .endObject()
                    .endObject();
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
            createIndexRequest.mapping(builder);
            createIndexRequest.settings(esSetting);
            CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            boolean acknowledged = createIndexResponse.isAcknowledged();
            if (acknowledged) {
                return new ResponseBean(HttpStatus.OK.value(), "创建成功", null);
            } else {
                return new ResponseBean(1002, "创建失败", null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResponseEntity saveUser(UserModel userModel) {
        IndexRequest request = new IndexRequest("user");
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("user_id", userModel.getUserId().toString())
                    .field("user_name", userModel.getUserName())
                    .field("phone", userModel.getPhone())
                    .field("address", userModel.getAddress())
                    .field("birthday", DateUtil.dateFormate(userModel.getBirthday(), "yyyy-MM-dd"))
                    .field("email", userModel.getEmail())
                    .field("id_card", userModel.getIdCard())
                    .field("create_time", DateUtil.dateFormate(userModel.getCreateTime(), "yyyy-MM-dd"))
                    .field("update_time", DateUtil.dateFormate(userModel.getUpdateTime(), "yyyy-MM-dd"))
                    .endObject();

            request.opType("index").source(builder);
            IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
            return new ResponseEntity(response.getId(), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<UserModel> queryUsers(String userName, String email, String phone, String address) {
        SearchRequest request = new SearchRequest("user");
        //构造bool查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (org.apache.commons.lang3.StringUtils.isNotEmpty(userName)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("user_name", userName));
        }

        if (!StringUtils.isEmpty(email)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("email", email));
        }

        if (!StringUtils.isEmpty(phone)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("phone", phone));
        }

        if (!StringUtils.isEmpty(address)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("address", address));
        }

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0).size(100).query(boolQueryBuilder);
        System.out.println(searchSourceBuilder);

        request.searchType(SearchType.DEFAULT).source(searchSourceBuilder);

        List<UserModel> userModelList = new ArrayList<>();
        try {
            for (SearchHit s : restHighLevelClient.search(request, RequestOptions.DEFAULT).getHits().getHits()) {
                UserModel userModel = new UserModel();
                userModel.setUserId(Long.valueOf(s.getSourceAsMap().get("user_id").toString()));
                userModel.setUserName(s.getSourceAsMap().get("user_name").toString());
                userModel.setPhone(s.getSourceAsMap().get("phone").toString());
                userModel.setAddress(s.getSourceAsMap().get("address").toString());
                userModel.setBirthday(new DateTime(s.getSourceAsMap().get("birthday").toString()).toDate());
                userModel.setEmail(s.getSourceAsMap().get("email").toString());
                userModel.setIdCard(s.getSourceAsMap().get("id_card").toString());
                userModel.setCreateTime(new DateTime(s.getSourceAsMap().get("create_time").toString()).toDate());
                userModel.setUpdateTime(new DateTime(s.getSourceAsMap().get("update_time").toString()).toDate());
                userModelList.add(userModel);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return userModelList;
    }
}

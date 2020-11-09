package com.elasticsearch.demo.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @program: elasticsearch-demo
 * @description:
 * @author: Reagan Li
 * @create: 2020-11-08 11:16
 **/

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class EsUtilTest {

    @Autowired
    private EsUtil esUtil;

    @Test
    public void testIsIndexExists() {
        Boolean bookExists = esUtil.isIndexExists("book");
        log.info("----- book index exists: {}", bookExists);

        Boolean peopleExists = esUtil.isIndexExists("people");
        log.info("----- people index exists: {}", peopleExists);

        Boolean userExists = esUtil.isIndexExists("user");
        log.info("----- user index exists: {}", userExists);
    }

    @Test
    public void testCreateIndex() {
        esUtil.createIndex("test_es");
    }

    @Test
    public void testDeleteIndex() {
        esUtil.deleteIndex("test_es");
    }
}

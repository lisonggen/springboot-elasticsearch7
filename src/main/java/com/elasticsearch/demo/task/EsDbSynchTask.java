package com.elasticsearch.demo.task;

import com.elasticsearch.demo.mapper.BookMapper;
import com.elasticsearch.demo.model.BookModel;
import com.elasticsearch.demo.util.EsUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @program: elasticsearch-demo
 * @description:
 * @author: Reagan Li
 * @create: 2020-11-05 08:36
 **/
@Slf4j
@Component
public class EsDbSynchTask {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private EsUtil esUtil;

//    @Scheduled(initialDelay=1000, fixedDelay=60000)
    public void esDbSynchronized() {
        List<BookModel> bookModelList = esUtil.queryBook(null,null,null,
                null,null,null,null);
        for (BookModel bookModel : bookModelList) {
            if (bookModel.getId() == null) {
                BookModel Model = new BookModel();
                Model.setAuth(bookModel.getAuth());
                Model.setWordCount(bookModel.getWordCount());
                Model.setTitle(bookModel.getTitle());
                Model.setPublishDate(bookModel.getPublishDate());
                bookMapper.insertSelective(bookModel);

                esUtil.update(bookModel);
            }
        }
    }

//    @Scheduled(initialDelay=1000, fixedDelay=60000)
    public void dbEsSynchronized() {
        List<BookModel> bookModelList = bookMapper.selectAll();
        for (BookModel model : bookModelList) {
            List<BookModel> bookModels = esUtil.queryBook(model.getId(), model.getAuth(),
                    model.getTitle(), null, null, null, null);
            if (bookModels.size() == 0) {
                esUtil.saveBook(model);
            }
        }
    }
}

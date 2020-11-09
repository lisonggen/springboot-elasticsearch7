package com.elasticsearch.demo.service.impl;

import com.elasticsearch.demo.mapper.BookMapper;
import com.elasticsearch.demo.model.BookModel;
import com.elasticsearch.demo.service.BookService;
import com.elasticsearch.demo.util.EsUtil;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @program: elasticsearch-demo
 * @description:
 * @author: Reagan Li
 * @create: 2020-11-03 22:20
 **/

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private EsUtil esUtil;

    @Override
    public List<BookModel> getBooks(Long id, String auth, String title) {
        return esUtil.queryBook(id, auth, title,
                null, null, null, null);
    }

    @Override
    @Transactional
    public ResponseEntity saveBook(BookModel bookModel) {
        bookMapper.insertSelective(bookModel);
        esUtil.saveBook(bookModel);
        return new ResponseEntity(bookModel, HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity deleteById(Long id) {
        BookModel bookModel = bookMapper.selectById(id);
        List<BookModel> bookModelList = esUtil.queryBook(bookModel.getId(), bookModel.getAuth(), bookModel.getTitle(),
                null, null,null, null);
        bookMapper.deleteById(id);
        for (BookModel model : bookModelList) {
            esUtil.deleteBook(model.getEsId());
        }
        return new ResponseEntity(HttpStatus.OK);
    }
}

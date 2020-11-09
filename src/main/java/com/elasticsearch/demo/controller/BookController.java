package com.elasticsearch.demo.controller;

import com.elasticsearch.demo.model.BookModel;
import com.elasticsearch.demo.service.BookService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @program: elasticsearch-demo
 * @description:
 * @author: Reagan Li
 * @create: 2020-11-04 08:25
 **/
@RestController
public class BookController {

    @Autowired
    private BookService bookService;

    @ApiOperation(value = "查询接口", notes = "按照id, auth, title查询book")
    @GetMapping("/book/get")
    public ResponseEntity getById(@RequestParam(name = "id", required = false) Long id,
                             @RequestParam(name = "auth", required = false) String auth,
                             @RequestParam(name = "title", required = false) String title) {
        List<BookModel> bookModelList = bookService.getBooks(id, auth, title);
        return new ResponseEntity(bookModelList, HttpStatus.OK);
    }

    @PostMapping("/book/save")
    public ResponseEntity add(@RequestBody BookModel bookModel) {
        ResponseEntity responseEntity =  bookService.saveBook(bookModel);
        return responseEntity;
    }

    @DeleteMapping("/book/delete")
    public ResponseEntity deleteById(@RequestParam(name = "id") Long id) {
        return bookService.deleteById(id);
    }
}

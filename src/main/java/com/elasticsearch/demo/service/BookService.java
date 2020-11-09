package com.elasticsearch.demo.service;

import com.elasticsearch.demo.model.BookModel;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface BookService {

    public List<BookModel> getBooks(Long id, String auth, String title);

    public ResponseEntity saveBook(BookModel bookModel);

    public ResponseEntity deleteById(Long id);

}

package com.elasticsearch.demo.mapper;

import com.elasticsearch.demo.model.BookModel;

import java.util.List;

public interface BookMapper {

    public BookModel selectById(Long id);

    public List<BookModel> selectAll();

    public Integer deleteById(Long id);

    public Integer insertSelective(BookModel bookModel);

    public Integer updateById(BookModel bookModel);
}

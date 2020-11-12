package com.elasticsearch.demo.mapper;

import com.elasticsearch.demo.model.UserModel;

import java.util.List;

public interface UserMapper {

    public UserModel selectById(Long id);

    public List<UserModel> selectAll();

    public Integer deleteById(Long id);

    public Integer insertSelective(UserModel userModel);

    public Integer updateById(UserModel userModel);
}

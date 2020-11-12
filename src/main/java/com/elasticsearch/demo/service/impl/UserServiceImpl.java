package com.elasticsearch.demo.service.impl;

import com.elasticsearch.demo.mapper.UserMapper;
import com.elasticsearch.demo.model.UserModel;
import com.elasticsearch.demo.service.UserService;
import com.elasticsearch.demo.util.EsUserUtil;
import com.elasticsearch.demo.util.SnowflakeIdWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @program: elasticsearch-demo
 * @description:
 * @author: Reagan Li
 * @create: 2020-11-09 22:28
 **/
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EsUserUtil esUserUtil;

    @Override
    public List<UserModel> getUsers(String userName, String email, String phone, String address) {
        return esUserUtil.queryUsers(userName, email, phone, address);
    }

    @Override
    @Transactional
    public ResponseEntity saveUser(UserModel userModel) {
        Date now = new Date();
        SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker();
        userModel.setUserId(snowflakeIdWorker.nextId());
        userModel.setCreateTime(now);
        userModel.setUpdateTime(now);
        userMapper.insertSelective(userModel);

        esUserUtil.saveUser(userModel);
        return new ResponseEntity(userModel, HttpStatus.OK);
    }

    @Override
    public ResponseEntity deleteById(Long id) {
        return null;
    }
}

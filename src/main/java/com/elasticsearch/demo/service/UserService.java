package com.elasticsearch.demo.service;

import com.elasticsearch.demo.model.UserModel;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {

    public List<UserModel> getUsers(String userName, String email, String phone, String address);

    public ResponseEntity saveUser(UserModel userModel);

    public ResponseEntity deleteById(Long id);

}

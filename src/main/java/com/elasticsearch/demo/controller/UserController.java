package com.elasticsearch.demo.controller;

import com.elasticsearch.demo.model.BookModel;
import com.elasticsearch.demo.model.UserModel;
import com.elasticsearch.demo.service.BookService;
import com.elasticsearch.demo.service.UserService;
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
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation(value = "查询接口", notes = "按照id, auth, title查询book")
    @GetMapping("/user/query")
    public ResponseEntity queryUsers(@RequestParam(name = "userName", required = false) String userName,
                             @RequestParam(name = "email", required = false) String email,
                             @RequestParam(name = "phone", required = false) String phone,
                             @RequestParam(name = "address", required = false) String address) {
        List<UserModel> userModelList = userService.getUsers(userName, email, phone, address);
        return new ResponseEntity(userModelList, HttpStatus.OK);
    }

    @PostMapping("/user/save")
    public ResponseEntity add(@RequestBody UserModel userModel) {
        ResponseEntity responseEntity = userService.saveUser(userModel);
        return responseEntity;
    }

    @DeleteMapping("/user/delete")
    public ResponseEntity deleteById(@RequestParam(name = "id") Long id) {
        return userService.deleteById(id);
    }
}

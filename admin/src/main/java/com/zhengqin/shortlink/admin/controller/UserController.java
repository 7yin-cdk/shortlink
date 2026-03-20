package com.zhengqin.shortlink.admin.controller;

import com.zhengqin.shortlink.admin.common.convention.result.Result;
import com.zhengqin.shortlink.admin.common.convention.result.Results;
import com.zhengqin.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.zhengqin.shortlink.admin.dto.resp.UserRespDTO;
import com.zhengqin.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制层
 * */
@RestController
@RequiredArgsConstructor
public class UserController {

    private  final UserService userService;

    /**
     *根据用户名查询用户信息
     */
    @GetMapping("/api/shortlink/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username){
        UserRespDTO userByUserame = userService.getUserByUserame(username);
            return Results.success(userByUserame);
        }
    }


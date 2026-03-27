package com.zhengqin.shortlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.zhengqin.shortlink.admin.common.convention.result.Result;
import com.zhengqin.shortlink.admin.common.convention.result.Results;
import com.zhengqin.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.zhengqin.shortlink.admin.dto.req.UserLoginReqDTO;
import com.zhengqin.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.zhengqin.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.zhengqin.shortlink.admin.dto.resp.ActualUserRespDTO;
import com.zhengqin.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.zhengqin.shortlink.admin.dto.resp.UserRespDTO;
import com.zhengqin.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/api/short-link/admin/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username){
        UserRespDTO userByUserame = userService.getUserByUserame(username);
            return Results.success(userByUserame);
    }

    /**
     * 根据用户名查询用户无脱敏信息
     * @param username
     */
    @GetMapping("/api/short-link/admin/v1/actual/user/{username}")
    public Result<ActualUserRespDTO> getActualUserByUsername(@PathVariable("username") String username){
        UserRespDTO userByUserame = userService.getUserByUserame(username);
        return Results.success(BeanUtil.toBean(userService.getUserByUserame(username),ActualUserRespDTO.class));
    }

    /**
     * 查询用户名是否可用
     * @param username
     */
    @GetMapping("/api/short-link/admin/v1/user/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") String username){
        return Results.success(!userService.hasUsername(username));
    }

    /**
     * 注册用户
     * @param userRegisterReqDTO
     */
    @PostMapping("/api/short-link/admin/v1/user")
    public Result<Void> register(@RequestBody UserRegisterReqDTO userRegisterReqDTO){
        userService.register(userRegisterReqDTO);
        return Results.success();
    }

    /**
     * 根据用户名修改用户
     * @param userUpdateReqDTO
     */
    @PutMapping("/api/short-link/admin/v1/user")
    public Result<Void> update(@RequestBody UserUpdateReqDTO userUpdateReqDTO){
        userService.update(userUpdateReqDTO);
        return Results.success();
    }

    /**
     * 用户登录
     * @param requestParam
     */
    @PostMapping("/api/short-link/admin/v1/user/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestParam){
        return Results.success(userService.login(requestParam));
    }

    /**
     * 检查用户是否登录
     * @param username
     * @param token
     */
    @GetMapping("/api/short-link/admin/v1/user/check-login")
    public Result<Boolean> checkLogin(@RequestParam("username")String username,@RequestParam("token")String token){
        return Results.success(userService.checkLogin(username,token));
    }

    /**
     * 用户退出登录
     * @param username
     * @param token
     */
    @DeleteMapping("/api/short-link/admin/v1/user/logout")
    public Result<Void> logout(@RequestParam("username")String username,@RequestParam("token")String token){
        userService.logout(username,token);
        return Results.success();
    }
}


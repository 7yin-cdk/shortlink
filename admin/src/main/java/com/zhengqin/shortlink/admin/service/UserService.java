package com.zhengqin.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhengqin.shortlink.admin.dao.entity.UserDO;
import com.zhengqin.shortlink.admin.dto.req.UserLoginReqDTO;
import com.zhengqin.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.zhengqin.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.zhengqin.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.zhengqin.shortlink.admin.dto.resp.UserRespDTO;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询返回用户响应实体
     * @param username
     * @return
     */
    UserRespDTO getUserByUserame(String username);

    /**
     * 查询用户名是否已经存在
     * @param username 用户名
     * @return 用户名存在返回True，不存在返回False
     */
    Boolean hasUsername(String username);

    /**
     * 用户注册
     *
     * @param requestParam 用户注册请求参数
     */
    void register(UserRegisterReqDTO requestParam);

    /**
     * 修改用户信息
     *
     * @param requestParam
     */
    void update(UserUpdateReqDTO requestParam);

    /**
     * 用户登录
     * @param requestParam 用户登录请求参数
     * @return 用户的登录返回参数Token
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    /**
     * 检查用户是否登录
     *
     * @param username
     * @param token    Token
     * @return
     */
    Boolean checkLogin(String username, String token);

    /**
     * 用户退出登录
     * @param username 用户名
     * @param token Token
     */
    void logout(String username, String token);
}

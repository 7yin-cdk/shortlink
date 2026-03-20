package com.zhengqin.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhengqin.shortlink.admin.dao.entity.UserDO;
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
}

package com.zhengqin.shortlink.admin.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录响应实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRespDTO {
    /**
     * Token
     */
    private String token;
}

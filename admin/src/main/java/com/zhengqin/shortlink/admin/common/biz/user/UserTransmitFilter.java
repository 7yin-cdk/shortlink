package com.zhengqin.shortlink.admin.common.biz.user;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.zhengqin.shortlink.admin.common.convention.exception.ClientException;
import com.zhengqin.shortlink.admin.common.enums.UserErrorCodeEnum;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 用户信息传输过滤器
 *
 * @公众号：马丁玩编程，回复：加群，添加马哥微信（备注：12306）获取项目资料
 */
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {
    
    private final StringRedisTemplate stringRedisTemplate;

    private final static List<String> IGNORE_URL = Lists.newArrayList(
            "/api/short-link/admin/v1/user/login",
            "/api/short-link/admin/v1/user/has-username");

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // TODO 责任链模式改造
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String ignoreURI = ((HttpServletRequest) servletRequest).getRequestURI();
        if(!IGNORE_URL.contains(ignoreURI)){
            String method = httpServletRequest.getMethod();
            if(!(Objects.equals(ignoreURI,"/api/short-link/admin/v1/user") && Objects.equals(method,"POST"))){
                String username = httpServletRequest.getHeader("username");
                String token = httpServletRequest.getHeader("token");
                if(StrUtil.isAllBlank(token,username)){
                    throw new ClientException(UserErrorCodeEnum.USER_TOKEN_FAIL);
                }
                Object userInfoJsonStr;
                try{
                    userInfoJsonStr = stringRedisTemplate.opsForHash().get("login_"+username, token);
                    if(Objects.isNull(userInfoJsonStr)){
                        throw new ClientException(UserErrorCodeEnum.USER_TOKEN_FAIL);
                    }
                }catch (Exception e){
                    throw new ClientException(UserErrorCodeEnum.USER_TOKEN_FAIL);
                }
                    UserInfoDTO userInfoDTO = JSON.parseObject(userInfoJsonStr.toString(), UserInfoDTO.class);
                    UserContext.setUser(userInfoDTO);
            }
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        }finally {
            UserContext.removeUser();
        }
    }
}
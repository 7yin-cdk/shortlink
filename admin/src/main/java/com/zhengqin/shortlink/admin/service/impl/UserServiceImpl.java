package com.zhengqin.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhengqin.shortlink.admin.common.constant.RedisCacheConstant;
import com.zhengqin.shortlink.admin.common.convention.exception.ClientException;
import com.zhengqin.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.zhengqin.shortlink.admin.dao.entity.UserDO;
import com.zhengqin.shortlink.admin.dao.mapper.UserMapper;
import com.zhengqin.shortlink.admin.dto.req.UserLoginReqDTO;
import com.zhengqin.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.zhengqin.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.zhengqin.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.zhengqin.shortlink.admin.dto.resp.UserRespDTO;
import com.zhengqin.shortlink.admin.service.GroupService;
import com.zhengqin.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static cn.hutool.core.date.DateTime.now;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> bloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final GroupService  groupService;

    @Override
    public UserRespDTO getUserByUserame(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        UserRespDTO result = new UserRespDTO();
        if (userDO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        BeanUtils.copyProperties(userDO, result);
        return result;
    }

    @Override
    public Boolean hasUsername(String username) {
        return bloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterReqDTO requestParam) {
        if(hasUsername(requestParam.getUsername())) {
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIT);
        }
        RLock lock = redissonClient.getLock(RedisCacheConstant.LOCK_USER_REGISTER_KEY + requestParam.getUsername());
        try{
            if(lock.tryLock()){
                UserDO user = BeanUtil.toBean(requestParam, UserDO.class);
                user.setCreateTime(now());
                user.setUpdateTime(now());
                user.setDelFlag(0);
                try{
                    int insert = baseMapper.insert(user);
                    if(insert < 1) {
                        throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);
                    }
                }catch (DuplicateKeyException e){
                    throw new ClientException(UserErrorCodeEnum.USER_EXIT);
                }
                bloomFilter.add(user.getUsername());
                groupService.saveGroup("默认分组");
                return;
            }
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIT);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void update(UserUpdateReqDTO requestParam) {
        LambdaUpdateWrapper<UserDO> eq = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername());
        UserDO user = BeanUtil.toBean(requestParam, UserDO.class);
        user.setUpdateTime(now());
        baseMapper.update(user,eq);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername())
                .eq(UserDO::getPassword, requestParam.getPassword())
                .eq(UserDO::getDelFlag, 0);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException("用户不存在");
        }
        Boolean hasLogin = stringRedisTemplate.hasKey("login_" + requestParam.getUsername());
        if(hasLogin != null && hasLogin){
            throw new ClientException("用户已登录");
        }
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put("login_"+requestParam.getUsername(), uuid, JSON.toJSONString(userDO));
        stringRedisTemplate.expire("login_"+requestParam.getUsername(), 30,TimeUnit.DAYS);
        return new UserLoginRespDTO(uuid);
    }

    @Override
    public Boolean checkLogin(String username, String token) {
        return stringRedisTemplate.opsForHash().hasKey("login_"+username,token);
    }

    @Override
    public void logout(String username, String token) {
        if(checkLogin(username,token)){
            stringRedisTemplate.opsForHash().delete("login_"+username,token);

        }else{
            throw new ClientException("用户Token错误或用户未登录");
        }
    }
}

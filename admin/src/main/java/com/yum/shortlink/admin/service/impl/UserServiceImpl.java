package com.yum.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yum.shortlink.admin.common.constants.RedisCacheConstants;
import com.yum.shortlink.admin.common.convention.exception.ClientException;
import com.yum.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.yum.shortlink.admin.dao.entity.UserDO;
import com.yum.shortlink.admin.dao.mapper.UserMapper;
import com.yum.shortlink.admin.dto.request.UserLoginReqDTO;
import com.yum.shortlink.admin.dto.request.UserRegisterReqDTO;
import com.yum.shortlink.admin.dto.request.UserUpdateReqDTO;
import com.yum.shortlink.admin.dto.response.UserLoginRespDTO;
import com.yum.shortlink.admin.dto.response.UserRespDTO;
import com.yum.shortlink.admin.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements IUserService {


    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    private final RedissonClient redissonClient;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 根据用户名查询用户信息
     */
    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO user = baseMapper.selectOne(queryWrapper);

        if (Objects.isNull(user)) {
            throw new ClientException(UserErrorCodeEnum.USER_NOT_EXIST);
        }

        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(user, result);
        return result;
    }

    /**
     * 查询用户名是否存在
     */
    @Override
    public Boolean hasUsername(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    /**
     * 用户注册
     */
    @Override
    public void registerUser(UserRegisterReqDTO requestParam) {

        // 检查用户名是否已经存在 -- 用户名全局唯一
        if (hasUsername(requestParam.getUsername())) {
            throw new ClientException(UserErrorCodeEnum.USER_ALREADY_EXIST);
        }

        RLock lock = redissonClient.getLock(RedisCacheConstants.LOCK_USER_REGISTER_KEY + requestParam.getUsername());

        try {
            if(lock.tryLock()) {
                int inserted = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
                if (inserted < 1) {
                    throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);
                }

                // 注册成功后将数据加入布隆过滤器
                userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
                return;
            }
            // 没申请到锁，说明该用户名已经正在被注册，直接返回用户名已存在
            throw new ClientException(UserErrorCodeEnum.USER_ALREADY_EXIST);
        } finally {
            lock.unlock();
        }

    }

    /**
     * 根据用户名修改用户信息
     */
    @Override
    public void updateUser(UserUpdateReqDTO requestParam) {
        // TODO: 验证当前用户名是否为登录用户
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername());
        baseMapper.update(BeanUtil.toBean(requestParam, UserDO.class), updateWrapper);
    }

    /**
     * 用户登录
     */
    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        // 查询用户名密码是否匹配，并且用户记录未被删除
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername())
                .eq(UserDO::getPassword, requestParam.getPassword())
                .eq(UserDO::getDelFlag, 0);
        UserDO user = baseMapper.selectOne(queryWrapper);
        if (Objects.isNull(user)) {
            throw new ClientException(UserErrorCodeEnum.USER_NOT_EXIST);
        }

        // 登录校验成功，发放token并存入redis
        // 用uuid作为token和redis的key
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue().set(uuid, JSON.toJSONString(user), 30L, TimeUnit.MINUTES);

        return new UserLoginRespDTO(uuid);

    }
}

package com.yum.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yum.shortlink.admin.common.convention.exception.ClientException;
import com.yum.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.yum.shortlink.admin.dao.entity.UserDO;
import com.yum.shortlink.admin.dao.mapper.UserMapper;
import com.yum.shortlink.admin.dto.request.UserRegisterReqDTO;
import com.yum.shortlink.admin.dto.response.UserRespDTO;
import com.yum.shortlink.admin.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements IUserService {

    private final RBloomFilter<String> bloomFilter;
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

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
     * @param username
     * @return
     */
    @Override
    public Boolean hasUsername(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    /**
     * 用户注册
     * @param requestParam
     */
    @Override
    public void registerUser(UserRegisterReqDTO requestParam) {

        // 检查用户名是否已经存在 -- 用户名全局唯一
        if (hasUsername(requestParam.getUsername())) {
            throw new ClientException(UserErrorCodeEnum.USER_ALREADY_EXIST);
        }

        int inserted = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
        if (inserted < 1) {
            throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);
        }

        // 注册成功后将数据加入布隆过滤器
        userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());


    }
}

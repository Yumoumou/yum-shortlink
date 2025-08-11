package com.yum.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yum.shortlink.admin.common.convention.exception.ClientException;
import com.yum.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.yum.shortlink.admin.dao.entity.UserDO;
import com.yum.shortlink.admin.dao.mapper.UserMapper;
import com.yum.shortlink.admin.dto.response.UserRespDTO;
import com.yum.shortlink.admin.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 用户接口实现层
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements IUserService {


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
}

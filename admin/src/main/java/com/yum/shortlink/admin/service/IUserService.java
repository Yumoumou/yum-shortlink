package com.yum.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yum.shortlink.admin.dao.entity.UserDO;
import com.yum.shortlink.admin.dto.request.UserLoginReqDTO;
import com.yum.shortlink.admin.dto.request.UserRegisterReqDTO;
import com.yum.shortlink.admin.dto.request.UserUpdateReqDTO;
import com.yum.shortlink.admin.dto.response.UserLoginRespDTO;
import com.yum.shortlink.admin.dto.response.UserRespDTO;

/**
 * 用户接口层
 */
public interface IUserService extends IService<UserDO> {

    /**
     * 根据用户名返回用户信息
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 查询用户名是否合法 -- 是否存在
     */
    Boolean hasUsername(String username);

    /**
     * 用户注册
     */
    void registerUser(UserRegisterReqDTO requestParam);

    /**
     * 根据用户名用户更新信息
     */
    void updateUser(UserUpdateReqDTO requestParam);

    /**
     * 用户登录
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    /**
     * 检查用户是否登录
     * @param token 用户登录token
     */
    Boolean checkLogin(String username, String token);

    /**
     * 退出登录
     * @param username
     * @param token
     */
    void logout(String username, String token);
}

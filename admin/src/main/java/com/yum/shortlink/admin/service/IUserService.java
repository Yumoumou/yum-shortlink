package com.yum.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yum.shortlink.admin.dao.entity.UserDO;
import com.yum.shortlink.admin.dto.response.UserRespDTO;

/**
 * 用户接口层
 */
public interface IUserService extends IService<UserDO> {

    /**
     * 根据用户名返回用户信息
     * @param username
     * @return
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 查询用户名是否合法 -- 是否存在
     * @param username
     * @return
     */
    Boolean hasUsername(String username);
}

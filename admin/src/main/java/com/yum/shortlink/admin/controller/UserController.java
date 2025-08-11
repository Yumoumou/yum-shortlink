package com.yum.shortlink.admin.controller;

import com.yum.shortlink.admin.dto.response.UserRespDTO;
import com.yum.shortlink.admin.service.IUserService;
import com.yum.shortlink.admin.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制层
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    /**
     * 根据用户名查询用户信息
     */
    @GetMapping("/api/shortlink/v1/user/{username}")
    public UserRespDTO getUserByUsername(@PathVariable("username") String username) {

        return userService.getUserByUsername(username);
    }
}

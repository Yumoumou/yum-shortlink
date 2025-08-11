package com.yum.shortlink.admin.controller;

import com.yum.shortlink.admin.common.convention.result.Result;
import com.yum.shortlink.admin.common.convention.result.Results;
import com.yum.shortlink.admin.dto.request.UserRegisterReqDTO;
import com.yum.shortlink.admin.dto.response.UserRespDTO;
import com.yum.shortlink.admin.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

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
    @GetMapping("/api/short-link/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username) {

        UserRespDTO result = userService.getUserByUsername(username);

        return Results.success(result);
    }

    /**
     * 查询用户名是否存在
     */
    @GetMapping("/api/short-link/v1/user/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") String username) {

        Boolean hasUsername = userService.hasUsername(username);

        return Results.success(hasUsername);
    }

    /**
     * 用户注册
     */
    @PostMapping("/api/short-link/v1/user")
    public Result<Void> registerUser(@RequestBody UserRegisterReqDTO requestParam) {

        userService.registerUser(requestParam);

        return Results.success();
    }




}

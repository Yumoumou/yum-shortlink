package com.yum.shortlink.admin.controller;

import com.yum.shortlink.admin.common.convention.result.Result;
import com.yum.shortlink.admin.common.convention.result.Results;
import com.yum.shortlink.admin.dto.request.UserLoginReqDTO;
import com.yum.shortlink.admin.dto.request.UserRegisterReqDTO;
import com.yum.shortlink.admin.dto.request.UserUpdateReqDTO;
import com.yum.shortlink.admin.dto.response.UserLoginRespDTO;
import com.yum.shortlink.admin.dto.response.UserRespDTO;
import com.yum.shortlink.admin.service.IGroupService;
import com.yum.shortlink.admin.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 用户管理控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin/v1/user")
public class UserController {

    private final IUserService userService;



    /**
     * 根据用户名查询用户信息
     */
    @GetMapping("/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username) {

        UserRespDTO result = userService.getUserByUsername(username);

        return Results.success(result);
    }

    /**
     * 查询用户名是否存在
     */
    @GetMapping("/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") String username) {

        Boolean hasUsername = userService.hasUsername(username);

        return Results.success(hasUsername);
    }

    /**
     * 用户注册
     */
    @PostMapping
    public Result<Void> registerUser(@RequestBody UserRegisterReqDTO requestParam) {

        userService.registerUser(requestParam);

        return Results.success();
    }

    /**
     * 用户更新信息
     */
    @PutMapping
    public Result<Void> updateUser(@RequestBody UserUpdateReqDTO requestParam) {

        userService.updateUser(requestParam);

        return Results.success();
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestParam) {

        UserLoginRespDTO result = userService.login(requestParam);

        return Results.success(result);
    }

    /**
     * 检查用户是否存在
     */
    @GetMapping("/check-login")
    public Result<Boolean> checkLogin(@RequestParam("username") String username, @RequestParam("token") String token) {

        Boolean result = userService.checkLogin(username, token);

        return Results.success(result);
    }

    /**
     * 退出登录
     */
    @DeleteMapping("/logout")
    public Result<Void> logout(@RequestParam("username") String username, @RequestParam("token") String token) {

        userService.logout(username, token);

        return Results.success();
    }




}

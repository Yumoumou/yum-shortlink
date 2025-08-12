package com.yum.shortlink.admin.dto.request;

import lombok.Data;

/**
 * 用户登录请求参数
 */
@Data
public class UserLoginReqDTO {

    private String username;

    private String password;
}

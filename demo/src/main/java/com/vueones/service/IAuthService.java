package com.vueones.service;

import com.vueones.dto.LoginRequest;
import com.vueones.dto.LoginResponse;
import com.vueones.dto.RegisterRequest;
import com.vueones.entity.User;

/**
 * 登录注册服务
 */
public interface IAuthService {

    /**
     * 用户登录
     * @param loginRequest 入参
     * @return 携带token的响应
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 用户注册
     * @param registerRequest 入参
     */
    void register(RegisterRequest registerRequest);

    /**
     * 校验token并返回用户
     * @param token JWT
     * @return token有效时返回用户信息
     */
    User validateToken(String token);
}


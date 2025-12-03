package com.vueones.service.impl;

import com.vueones.dto.LoginRequest;
import com.vueones.dto.LoginResponse;
import com.vueones.dto.RegisterRequest;
import com.vueones.entity.User;
import com.vueones.exception.AuthException;
import com.vueones.mapper.UserMapper;
import com.vueones.service.IAuthService;
import com.vueones.utils.JwtUtils;
import com.vueones.utils.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 登录注册领域服务
 */
@Service
public class AuthServiceImpl implements IAuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${app.security.login-verify:true}")
    private boolean loginVerifyEnabled;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        if (loginRequest == null || !StringUtils.hasText(loginRequest.getEmail())) {
            throw new AuthException(400, "邮箱不能为空");
        }
        if (loginVerifyEnabled && !StringUtils.hasText(loginRequest.getPassword())) {
            throw new AuthException(400, "密码不能为空");
        }

        User user = userMapper.selectByEmail(loginRequest.getEmail());
        if (user == null) {
            throw new AuthException(401, "账户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new AuthException(403, "账户已被禁用");
        }
        if (loginRequest.getUserType() != null && user.getUserType() != null
                && !user.getUserType().equals(loginRequest.getUserType())) {
            throw new AuthException(401, "账户类型不匹配");
        }

        if (loginVerifyEnabled) {
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                throw new AuthException(401, "密码错误");
            }
        }

        userMapper.updateLastLoginTime(user.getId());
        String token = jwtUtils.generateToken(user.getId(), user.getUserType());
        return new LoginResponse(user, token);
    }

    @Override
    public void register(RegisterRequest registerRequest) {
        if (registerRequest == null || !StringUtils.hasText(registerRequest.getEmail())) {
            throw new AuthException(400, "邮箱不能为空");
        }
        if (!StringUtils.hasText(registerRequest.getName())) {
            throw new AuthException(400, "用户名不能为空");
        }
        if (!StringUtils.hasText(registerRequest.getPassword())) {
            throw new AuthException(400, "密码不能为空");
        }

        User existing = userMapper.selectByEmail(registerRequest.getEmail());
        if (existing != null) {
            throw new AuthException(400, "该邮箱已注册");
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail().trim());
        user.setName(registerRequest.getName().trim());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setUserType(registerRequest.getUserType() == null ? 0 : registerRequest.getUserType());
        user.setStatus(1);

        int inserted = userMapper.insert(user);
        if (inserted <= 0) {
            throw new AuthException(500, "注册失败，请稍后重试");
        }
        String channel = StringUtils.hasText(registerRequest.getRegisterChannel()) ? registerRequest.getRegisterChannel() : "web";
        userMapper.insertRegisterRecord(user.getEmail(), user.getName(), user.getUserType(),
                registerRequest.getRegisterIp(), channel);
    }

    @Override
    public User validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        Integer userId = jwtUtils.getUserIdFromToken(token);
        if (userId == null) {
            return null;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        boolean valid = jwtUtils.validateToken(token, user.getId());
        return valid ? user : null;
    }
}


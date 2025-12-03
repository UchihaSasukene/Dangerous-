package com.vueones.controller;

import com.vueones.dto.LoginRequest;
import com.vueones.dto.LoginResponse;
import com.vueones.dto.RegisterRequest;
import com.vueones.entity.User;
import com.vueones.exception.AuthException;
import com.vueones.service.IAuthService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
@Log4j2
public class UserController {

    @Autowired
    private IAuthService authService;

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 登录结果
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            log.info("接收到登录请求：{}", loginRequest);
            
            LoginResponse loginResponse = authService.login(loginRequest);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "登录成功");
            response.put("data", loginResponse);
            
            return ResponseEntity.ok(response);
        } catch (AuthException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", e.getStatus());
            error.put("message", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(error);
        } catch (Exception e) {
            log.error("登录失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "登录失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            log.info("接收到注册请求：{}", registerRequest);
            
            authService.register(registerRequest);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "注册成功");
            return ResponseEntity.ok(response);
        } catch (AuthException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", e.getStatus());
            error.put("message", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(error);
        } catch (Exception e) {
            log.error("注册失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "注册失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 检查token有效性
     * @param token JWT令牌
     * @return 检查结果
     */
    @GetMapping("/check-token")
    public ResponseEntity<?> checkToken(@RequestParam String token) {
        try {
            User user = authService.validateToken(token);
            if (user == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 401);
                error.put("message", "无效的令牌");
                return ResponseEntity.status(401).body(error);
            }
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            data.put("user", user);
            response.put("code", 200);
            response.put("message", "有效的令牌");
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("检查令牌失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 401);
            error.put("message", "无效的令牌");
            return ResponseEntity.status(401).body(error);
        }
    }
} 
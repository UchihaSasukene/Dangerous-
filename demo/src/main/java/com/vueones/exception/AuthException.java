package com.vueones.exception;

/**
 * 登录/注册相关的业务异常
 */
public class AuthException extends RuntimeException {

    private final int status;

    public AuthException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}


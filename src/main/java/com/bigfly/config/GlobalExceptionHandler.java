package com.bigfly.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public Map<String, Object> handleNotLoginException(NotLoginException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", "未登录或登录已过期");
        return result;
    }

    /**
     * 处理无权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public Map<String, Object> handleNotPermissionException(NotPermissionException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 403);
        result.put("message", e.getMessage());
        return result;
    }

    /**
     * 处理无角色异常
     */
    @ExceptionHandler(NotRoleException.class)
    public Map<String, Object> handleNotRoleException(NotRoleException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 403);
        result.put("message", e.getMessage());
        return result;
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleException(Exception e) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 500);
        result.put("message", e.getMessage());
        return result;
    }
}

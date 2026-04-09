package com.bigfly.common;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BigFlyException.class)
    public Result<?> handleBigFlyException(BigFlyException e) {
        String message = I18nUtils.getMessage(e.getMessageKey(), e.getArgs());
        return new Result<>(e.getCode(), message, null);
    }

    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLoginException(NotLoginException e) {
        return Result.unauthorized(I18nUtils.getMessage("401"));
    }

    @ExceptionHandler(NotPermissionException.class)
    public Result<?> handleNotPermissionException(NotPermissionException e) {
        String perm = e.getMessage().replace("没有权限：", "").replace("No permission: ", "");
        String message = I18nUtils.getMessage("404", perm);
        return new Result<>(404, message, null);
    }

    @ExceptionHandler(NotRoleException.class)
    public Result<?> handleNotRoleException(NotRoleException e) {
        String role = e.getMessage().replace("没有角色：", "").replace("No role: ", "");
        String message = I18nUtils.getMessage("405", role);
        return new Result<>(405, message, null);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.error(I18nUtils.getMessage("500"));
    }
}

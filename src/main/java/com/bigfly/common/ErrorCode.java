package com.bigfly.common;

import lombok.Getter;

/**
 * 异常码枚举
 */
@Getter
public enum ErrorCode {

    SUCCESS(0),
    SYSTEM_ERROR(500),
    PARAM_ERROR(501),
    DATA_NOT_FOUND(502),
    UNAUTHORIZED(401),
    TOKEN_EXPIRED(402),
    FORBIDDEN(403),
    PERMISSION_DENIED(404),
    ROLE_DENIED(405),
    USER_NOT_EXIST(1001),
    USER_PASSWORD_ERROR(1002),
    USER_DISABLED(1003),
    USER_ALREADY_EXISTS(1004);

    private final Integer code;

    ErrorCode(Integer code) {
        this.code = code;
    }
}

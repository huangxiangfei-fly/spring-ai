package com.bigfly.common;

import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class BigFlyException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误信息key（用于国际化）
     */
    private String messageKey;

    /**
     * 错误信息参数
     */
    private Object[] args;

    public BigFlyException(String messageKey) {
        super(messageKey);
        this.code = 500;
        this.messageKey = messageKey;
        this.args = null;
    }

    public BigFlyException(Integer code, String messageKey) {
        super(messageKey);
        this.code = code;
        this.messageKey = messageKey;
        this.args = null;
    }

    public BigFlyException(ErrorCode errorCode) {
        super(String.valueOf(errorCode.getCode()));
        this.code = errorCode.getCode();
        this.messageKey = String.valueOf(errorCode.getCode());
        this.args = null;
    }

    public BigFlyException(ErrorCode errorCode, Object... args) {
        super(String.valueOf(errorCode.getCode()));
        this.code = errorCode.getCode();
        this.messageKey = String.valueOf(errorCode.getCode());
        this.args = args;
    }

    public BigFlyException(String messageKey, Object... args) {
        super(messageKey);
        this.code = 500;
        this.messageKey = messageKey;
        this.args = args;
    }

    public BigFlyException(Integer code, String messageKey, Object... args) {
        super(messageKey);
        this.code = code;
        this.messageKey = messageKey;
        this.args = args;
    }

    public BigFlyException(String messageKey, Throwable cause) {
        super(messageKey, cause);
        this.code = 500;
        this.messageKey = messageKey;
        this.args = null;
    }

    public BigFlyException(Integer code, String messageKey, Throwable cause, Object... args) {
        super(messageKey, cause);
        this.code = code;
        this.messageKey = messageKey;
        this.args = args;
    }
}

package com.cyanrocks.boilerplate.constants;

import com.cyanrocks.common.em.AppErrorCode;

public enum ErrorCodeEnum {

    /**
     * 登陆时用户名或者密码错误
     */
    USERNAME_OR_PASSWORD_ERROR(AppErrorCode.USER.getCode() + "100001"),

    /**
     * 邮箱注册时 邮箱账号已经存在
     */
    EMAIL_ACCOUNT_ALREADY_EXIST(AppErrorCode.USER.getCode() + "100002"),

    /**
     * 手机注册时 手机账号已经存在
     */
    PHONE_ACCOUNT_ALREADY_EXIST(AppErrorCode.USER.getCode() + "100003"),

    /**
     * 账号不存在
     */
    USER_ACCOUNT_NOT_EXIST(AppErrorCode.USER.getCode() + "100004"),

    /**
     * 验证码不匹配
     */
    VALIDATE_CODE_NOT_MATCH(AppErrorCode.USER.getCode() + "100005"),

    /**
     * 验证码不存在或者已过期
     */
    VALIDATE_CODE_NOT_EXIST_OR_EXPIRED(AppErrorCode.USER.getCode() + "100006"),

    /**
     * 密码错误
     */
    CREDENTIAL_ERROR(AppErrorCode.USER.getCode() + "100007"),

    /**
     * 修改密码时 新旧密码不匹配
     */
    ORIGINAL_CREDENTIAL_NOT_MATCH(AppErrorCode.USER.getCode() + "100008"),

    /**
     * 无效的验证码
     */
    BAD_VALIDATE_CODE_EXCEPTION(AppErrorCode.USER.getCode() + "100009"),

    /**
     * 验证码发送太频繁
     */
    VALIDATE_CODE_SENT_TOO_OFTEN(AppErrorCode.USER.getCode() + "100010"),

    /**
     * 用户session过期，登陆状态失效
     */
    SESSION_EXPIRED(AppErrorCode.USER.getCode() + "100011"),

    /**
     * 无效的sessionId
     */
    SESSION_INVALID(AppErrorCode.USER.getCode() + "100012"),

    /**
     * 用户未登陆
     */
    USER_NOT_LOGIN(AppErrorCode.USER.getCode() + "100013"),
    ;


    private final String code;

    ErrorCodeEnum(String code) {
        this.code = code;
    }

    public Integer getCode() {
        return Integer.valueOf(code);
    }
}

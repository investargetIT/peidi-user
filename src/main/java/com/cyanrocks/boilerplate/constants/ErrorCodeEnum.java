package com.cyanrocks.boilerplate.constants;

import com.cyanrocks.common.em.AppErrorCode;

public enum ErrorCodeEnum {
    /**
     *
     */
    REPEAT_PARAM(AppErrorCode.USER.getCode() + "100000"),

    ;


    private final String code;

    ErrorCodeEnum(String code) {
        this.code = code;
    }

    public Integer getCode() {
        return Integer.valueOf(code);
    }
}

package com.cyanrocks.boilerplate.validate.service;


import com.cyanrocks.boilerplate.constants.ValidateCodeTypeEnum;

/**
 * @Author lmh
 * @Date 2020/7/30 3:15 下午
 * @Version 1.0
 **/
public class ValidateCode {

    private String code;

    private Long activeTimeMs;

    private ValidateCodeTypeEnum type;

    public ValidateCode(String code, Long activeTimeMs, ValidateCodeTypeEnum type) {
        this.code = code;
        this.activeTimeMs = activeTimeMs;
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getActiveTimeMs() {
        return activeTimeMs;
    }

    public void setActiveTimeMs(Long activeTimeMs) {
        this.activeTimeMs = activeTimeMs;
    }

    public ValidateCodeTypeEnum getType() {
        return type;
    }

    public void setType(ValidateCodeTypeEnum type) {
        this.type = type;
    }
}

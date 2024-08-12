package com.cyanrocks.boilerplate.user.model.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@Data
public class RegisterRequest {

    @NotBlank
    private String authType;

    @NotBlank
    private String identifier;

    @NotBlank
//    @ValidPassword
    private String password;

    @NotBlank
//    @ValidPassword
    private String confirmPassword;

    private String referralCode;

    @NotNull
    private Boolean agreePrivacyPolicy;

    @NotBlank
    private String verifyCode;
}


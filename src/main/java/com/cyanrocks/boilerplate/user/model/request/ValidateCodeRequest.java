package com.cyanrocks.boilerplate.user.model.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@Data
public class ValidateCodeRequest {

    @NotNull
    @ApiModelProperty(value = "验证码发送的目的地|手机号或者邮箱", required = true, dataType = "String")
    private String destination;

    @NotNull
    @ApiModelProperty(value = "验证码类型", required = true, allowableValues = "email_register,email_reset_password",
        dataType = "String")
    private String codeType;
}

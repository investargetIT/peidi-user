package com.cyanrocks.boilerplate.vo.request;

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
    @ApiModelProperty(value = "验证码类型", required = true, allowableValues = "sms_register,sms_reset_password,sms_oms_order_trade",
        dataType = "String")
    private String codeType;
}

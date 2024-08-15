package com.cyanrocks.boilerplate.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@Data
@ApiModel(value = "用户忘记密码传参实体")
public class ForgetPasswordRequest {

    @NotNull
    @ApiModelProperty(value = "邮箱/手机号", required = true, dataType = "String")
    private String identifier;

    @NotNull
    @ApiModelProperty(value = "新密码", required = true, dataType = "String")
    private String newPassword;

    @NotNull
    @ApiModelProperty(value = "邮箱或者手机验证码", required = true, dataType = "String")
    private String validateCode;
}

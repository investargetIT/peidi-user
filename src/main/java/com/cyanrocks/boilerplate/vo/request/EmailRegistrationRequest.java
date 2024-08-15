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
@ApiModel(value = "邮箱注册实体参数")
public class EmailRegistrationRequest {

    @NotNull
    @ApiModelProperty(value = "邮箱", required = true, dataType = "String")
    private String email;

    @NotNull
    @ApiModelProperty(value = "邮箱验证码", required = true, dataType = "String")
    private String emailCode;

    @ApiModelProperty(value = "用户名", required = true, dataType = "String")
    private String username;

    @ApiModelProperty(value = "密码", required = true, dataType = "String")
    private String password;

}

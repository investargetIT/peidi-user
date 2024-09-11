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
    @ApiModelProperty(value = "邮箱")
    private String email;

    @NotNull
    @ApiModelProperty(value = "邮箱验证码")
    private String emailCode;

    @NotNull
    @ApiModelProperty(value = "用户名")
    private String username;

    @NotNull
    @ApiModelProperty(value = "密码")
    private String password;

}

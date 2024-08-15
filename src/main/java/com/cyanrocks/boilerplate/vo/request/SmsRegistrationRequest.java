package com.cyanrocks.boilerplate.vo.request;

import com.cyanrocks.boilerplate.validate.validator.ValidPhonePattern;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@Data
@ApiModel(value = "手机注册实体参数")
public class SmsRegistrationRequest {

    @ValidPhonePattern
    @NotNull
    @ApiModelProperty(value = "手机号", required = true, dataType = "String")
    private String mobile;

    @NotNull
    @ApiModelProperty(value = "手机验证码", required = true, dataType = "String")
    private String mobileCode;

    @ApiModelProperty(value = "用户名", required = true, dataType = "String")
    private String username;

    @ApiModelProperty(value = "密码", required = true, dataType = "String")
    private String password;

}

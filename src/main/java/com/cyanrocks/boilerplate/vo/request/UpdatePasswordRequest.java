package com.cyanrocks.boilerplate.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 用于用户在知道旧密码的情况下进行修改密码
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@Data
@ApiModel(value = "用户更新密码传参实体")
public class UpdatePasswordRequest {

    @NotNull
    @ApiModelProperty(value = "邮箱/手机号", required = true, dataType = "String")
    private String identifier;

    @NotNull
    @ApiModelProperty(value = "旧密码", required = true, dataType = "String")
    private String oldPassword;

    @NotNull
    @ApiModelProperty(value = "新密码", required = true, dataType = "String")
    private String newPassword;
}

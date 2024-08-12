package com.cyanrocks.boilerplate.user.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@Data
@ApiModel(value = "更新用户信息的参数实体")
public class UpdateUserInfoRequest {

    @ApiModelProperty(value = "用户是否展示新手教程")
    @NotNull
    private Boolean showTutorial;

    @ApiModelProperty(value = "用户主键ID")
    private Long userId;

}

package com.cyanrocks.boilerplate.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@ApiModel("用户信息的实体")
@Data
public class UserInfoVO {

    @ApiModelProperty(value = "用户账号")
    private String userName;

    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "用户的邮箱")
    private String email;

}

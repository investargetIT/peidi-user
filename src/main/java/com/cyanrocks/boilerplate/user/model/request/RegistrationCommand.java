package com.cyanrocks.boilerplate.user.model.request;

import com.tobee.brains.user.validate.validator.PasswordMatches;
import com.tobee.brains.user.validate.validator.ValidPassword;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@Data
@PasswordMatches
public class RegistrationCommand {

    @ValidPassword
    @ApiModelProperty(value = "密码", required = true, dataType = "String")
    private String password;

    @ValidPassword
    @ApiModelProperty(value = "确认密码", required = true, dataType = "String")
    private String matchingPassword;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMatchingPassword() {
        return matchingPassword;
    }

    public void setMatchingPassword(String matchingPassword) {
        this.matchingPassword = matchingPassword;
    }

}

package com.cyanrocks.boilerplate.user.model.vo;

import lombok.Data;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@Data
public class UserCheckVO {

    private Boolean hasLoginUser;
    private String identifier;

}

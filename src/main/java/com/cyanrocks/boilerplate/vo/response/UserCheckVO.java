package com.cyanrocks.boilerplate.vo.response;

import lombok.Data;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
@Data
public class UserCheckVO {

    private Long id;
    private Boolean hasLoginUser;
    private String username;
    private String service;
    private Long dataSource;
    private String deptId;

}

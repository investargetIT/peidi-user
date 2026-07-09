package com.cyanrocks.boilerplate.vo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.List;

/**
 * 钉钉用户信息DTO
 */
@Data
public class DingUserDTO {

    private String extension;

    private Boolean boss;

    private String unionid;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private String create_time;

    private Boolean exclusive_account;

    private String manager_userid;

    private String mobile;

    private Boolean active;

    private Boolean admin;

    private String avatar;

    private Boolean hide_mobile;

    private String hired_date;

    private String title;

    private String userid;

    private Boolean senior;

    private String work_place;

    private List<DeptOrder> dept_order_list;

    private Boolean real_authed;

    private String name;

    private List<Long> dept_id_list;

    private String job_number;

    private String state_code;

    private String email;

    private List<LeaderInDept> leader_in_dept;

    /**
     * 部门排序信息
     */
    @Data
    public static class DeptOrder {
        private Long dept_id;
        private String order;
    }

    /**
     * 部门领导信息
     */
    @Data
    public static class LeaderInDept {
        private Boolean leader;
        private Long dept_id;
    }
}
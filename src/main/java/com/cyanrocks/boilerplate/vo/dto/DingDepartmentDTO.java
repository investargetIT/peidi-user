package com.cyanrocks.boilerplate.vo.dto;

import lombok.Data;

import java.util.List;

/**
 * 钉钉部门信息DTO
 */
@Data
public class DingDepartmentDTO {

    private List<Object> dept_permits;

    private String extention;

    private List<Object> outer_permit_users;

    private Boolean emp_apply_join_dept;

    private Boolean outer_dept;

    private Boolean auto_approve_apply;

    private Boolean group_contain_sub_dept;

    private Boolean auto_add_user;

    private List<String> dept_manager_userid_list;

    private Integer owning_member_count;

    private Long parent_id;

    private Boolean hide_dept;

    private String name;

    private List<Object> outer_permit_depts;

    private List<Object> user_permits;

    private Long dept_id;

    private Integer member_count;

    private Boolean create_dept_group;

    private Integer order;
}
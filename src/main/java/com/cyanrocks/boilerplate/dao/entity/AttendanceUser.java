package com.cyanrocks.boilerplate.dao.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * 考勤组参与人员（工时看板）
 * @Author yangshihao
 * @Date 2026/8/18
 */
@Entity
@Table(name = "attendance_user")
@Data
public class AttendanceUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 钉钉userId */
    @Column(name = "ding_user_id", length = 64, unique = true)
    private String dingUserId;

    /** 考勤组ID */
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;


    /** 用户姓名,如果通过过来的用户从来没有登陆过系统,那就从dd获取姓名 */
    @Column(name = "other_name", length = 64, unique = true)
    private String otherName;

    /** 所属钉钉部门ID */
    @Column(name = "dept_id")
    private Long deptId;

    /** 所属部门名称 */
    @Column(name = "dept_name", length = 128)
    private String deptName;

}

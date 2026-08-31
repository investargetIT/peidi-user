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
 * 钉钉部门架构（工时看板 支线逻辑2）
 * 通过 /topapi/v2/department/listsub 从根部门(1)递归构建整棵部门树
 * @Author yangshihao
 * @Date 2026/8/20
 */
@Entity
@Table(name = "attendance_dept")
@Data
public class AttendanceDept {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 钉钉部门ID（唯一） */
    @Column(name = "dept_id", unique = true)
    private Long deptId;

    /** 部门名称 */
    @Column(name = "dept_name", length = 128)
    private String deptName;

    /** 父部门ID（根部门为1） */
    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

}

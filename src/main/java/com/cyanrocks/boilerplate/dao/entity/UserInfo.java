package com.cyanrocks.boilerplate.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @Author wjq
 * @Date 2025/4/28 14:03
 */
@Entity
@Table(name = "user_info")
@Data
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    @Comment("user表id")
    private Long userId;

    @Column(name = "emp_no")
    @Comment("员工编号，集团唯一")
    private Long empNo;

    @Column(name = "full_name")
    @Comment("姓名")
    private String fullName;

    @Column(name = "mobile_phone")
    @Comment("手机号")
    private String mobilePhone;

    @Column(name = "email")
    @Comment("公司邮箱")
    private String email;

    @Column(name = "avatar_url")
    @Comment("用户头像 OSS")
    private String avatarUrl;

    @Column(name = "dept_id")
    @Comment("department部门表id")
    private Long deptId;

    @Column(name = "job_title")
    @Comment("职务/岗位")
    private String jobTitle;

    @Column(name = "hire_date")
    @Comment("入职日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    @Column(name = "status")
    @Comment("人员状态")
    private Long status;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Transient
    @TableField(exist = false)
    private String hireDateStr;

}
package com.cyanrocks.boilerplate.dao.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.LocalDateTime;

/**
 * 加班审批记录（工时看板 支线逻辑1）
 * 来源：钉钉加班申请审批（processCode=PROC-E955D439-1EF2-4DA6-AB61-4F286F2F474F）
 * 一条加班审批包含多个加班人时，按人拆分为多行（实例ID+人 唯一）
 * @Author yangshihao
 * @Date 2026/8/20
 */
@Entity
@Table(name = "attendance_overtime",
        uniqueConstraints = @UniqueConstraint(columnNames = {"process_instance_id", "ding_user_id"}))
@Data
public class AttendanceOvertime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 审批实例ID（与dingUserId组合唯一，upsert依据） */
    @Column(name = "process_instance_id", length = 64)
    private String processInstanceId;

    /** 加班人钉钉userId（表单partner组件extValue里的emplId） */
    @Column(name = "ding_user_id", length = 64)
    private String dingUserId;

    /** 加班人姓名 */
    @Column(name = "username", length = 64)
    private String username;

    /** 加班开始时间 */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /** 加班结束时间 */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /** 加班时长（秒） */
    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

}

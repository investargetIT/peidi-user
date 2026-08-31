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
 * 钉钉打卡结果记录（工时看板）
 * 对应接口 /attendance/list 返回的 recordresult
 * @Author yangshihao
 * @Date 2026/8/18
 */
@Entity
@Table(name = "attendance_record")
@Data
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 钉钉打卡记录ID（唯一） */
    @Column(name = "record_id", unique = true)
    private Long recordId;

    /** 排班ID */
    @Column(name = "plan_id")
    private Long planId;

    /** 打卡人钉钉userId */
    @Column(name = "ding_user_id", length = 64)
    private String dingUserId;



    /** 考勤组ID */
    @Column(name = "group_id")
    private Long groupId;

    /** 工作日 */
    @Column(name = "work_date")
    private LocalDateTime workDate;

    /** 考勤类型：OnDuty-上班 OffDuty-下班 */
    @Column(name = "check_type", length = 16)
    private String checkType;

    /** 打卡结果：Normal-正常 Early-早退 Late-迟到 SeriousLate-严重迟到 Absenteeism-旷工迟到 NotSigned-未打卡 */
    @Column(name = "time_result", length = 32)
    private String timeResult;

    /** 位置结果：Normal-范围内 Outside-范围外 NotSigned-未打卡 */
    @Column(name = "location_result", length = 32)
    private String locationResult;

    /** 基准时间（计算迟到早退的基准） */
    @Column(name = "base_check_time")
    private LocalDateTime baseCheckTime;

    /** 实际打卡时间 */
    @Column(name = "user_check_time")
    private LocalDateTime userCheckTime;

    /** 数据来源：ATM-考勤机 USER-用户打卡 BOSS-老板改签 APPROVE-审批 SYSTEM-考勤系统 AUTO_CHECK-自动打卡 */
    @Column(name = "source_type", length = 32)
    private String sourceType;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}

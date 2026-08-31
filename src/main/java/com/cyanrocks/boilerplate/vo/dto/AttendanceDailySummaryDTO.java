package com.cyanrocks.boilerplate.vo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日工时汇总DTO（工时看板）
 */
@Data
public class AttendanceDailySummaryDTO {

    /** 钉钉userId */
    private String dingUserId;

    /** 用户姓名（已登录系统取user表，未登录取attendance_user.other_name） */
    private String username;

    /** 所属钉钉部门ID */
    private Long deptId;

    /** 所属部门名称 */
    private String deptName;

    /** 工作日 */
    private LocalDate workDay;

    /** 上班打卡时间 */
    private LocalDateTime onDutyTime;

    /** 下班打卡时间 */
    private LocalDateTime offDutyTime;

    /** 上班打卡结果：Normal-正常 Late-迟到 SeriousLate-严重迟到 Absenteeism-旷工迟到 NotSigned-未打卡 */
    private String onDutyTimeResult;

    /** 下班打卡结果：Normal-正常 Early-早退 SeriousLate-严重迟到 Absenteeism-旷工迟到 NotSigned-未打卡 */
    private String offDutyTimeResult;

    /** 工时（秒） */
    private Long durationSeconds;

    /** 工时（小时，保留2位） */
    private BigDecimal durationHours;

    /** 是否加班（当日存在加班审批为true） */
    private Boolean overtime;

    /** 当日加班开始时间（多条加班取最早），无加班为null */
    private LocalDateTime overtimeStartTime;

    /** 当日加班结束时间（多条加班取最晚），无加班为null */
    private LocalDateTime overtimeEndTime;

    /** 是否请假（当日存在请假记录为true） */
    private Boolean leave;

    /** 当日请假开始时间（多条请假取最早），无请假为null */
    private LocalDateTime leaveStartTime;

    /** 当日请假结束时间（多条请假取最晚），无请假为null */
    private LocalDateTime leaveEndTime;
}

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
 * 请假记录（工时看板 支线逻辑3）
 * 来源：钉钉 /topapi/attendance/getleavestatus 返回的 leave_status
 * 一行对应一条请假记录，不做事长计算，汇总查询时按「请假区间与工作日有交集」判断是否请假
 * @Author yangshihao
 * @Date 2026/8/24
 */
@Entity
@Table(name = "attendance_leave",
        uniqueConstraints = @UniqueConstraint(columnNames = {"ding_user_id", "leave_start_time", "leave_end_time", "leave_code"}))
@Data
public class AttendanceLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 请假人钉钉userId */
    @Column(name = "ding_user_id", length = 64)
    private String dingUserId;

    /** 请假开始时间 */
    @Column(name = "leave_start_time")
    private LocalDateTime leaveStartTime;

    /** 请假结束时间 */
    @Column(name = "leave_end_time")
    private LocalDateTime leaveEndTime;

    /** 请假类型标识（钉钉假勤code，UUID） */
    @Column(name = "leave_code", length = 64)
    private String leaveCode;

    /** 时长单位：percent_day-按天 percent_hour-按小时 */
    @Column(name = "duration_unit", length = 16)
    private String durationUnit;

    /** 请假时长占比（按天:值/100=天数，按小时:值/100=小时数） */
    @Column(name = "duration_percent")
    private Integer durationPercent;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

}

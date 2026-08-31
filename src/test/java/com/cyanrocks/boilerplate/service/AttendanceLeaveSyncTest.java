//package com.cyanrocks.boilerplate.service;
//
//import com.baomidou.mybatisplus.core.toolkit.Wrappers;
//import com.cyanrocks.boilerplate.dao.entity.AttendanceLeave;
//import com.cyanrocks.boilerplate.dao.mapper.AttendanceLeaveMapper;
//import com.cyanrocks.boilerplate.vo.dto.AttendanceDailySummaryDTO;
//import com.cyanrocks.boilerplate.vo.response.PageResult;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//
///**
// * 请假信息同步测试（真实调用钉钉接口，写入数据库）
// * 前置条件：
// * 1. 数据库已建表 attendance_leave
// * 2. attendance_user 表已有考勤组人员（可先跑 AttendanceUserSyncTest）
// */
//@SpringBootTest
//public class AttendanceLeaveSyncTest {
//
//    private static final Logger LOG = LoggerFactory.getLogger(AttendanceLeaveSyncTest.class);
//
//    /** 直接改这里：要同步的日期范围（含首尾，跨度不限，内部自动按180天切分） */
//    private static final LocalDate SYNC_START_DATE = LocalDate.of(2026, 3, 1);
//    private static final LocalDate SYNC_END_DATE = LocalDate.of(2026, 8, 25);
//
//    /** 直接改这里：每日工时汇总分页查询的日期范围 */
//    private static final LocalDate SUMMARY_START_DATE = LocalDate.of(2026, 3, 1);
//    private static final LocalDate SUMMARY_END_DATE = LocalDate.of(2026, 8, 25);
//
//    @Autowired
//    private AttendanceLeaveService attendanceLeaveService;
//
//    @Autowired
//    private AttendanceLeaveMapper attendanceLeaveMapper;
//
//    @Autowired
//    private AttendanceRecordService attendanceRecordService;
//
//    /**
//     * 同步指定日期范围内的请假记录并打印入库数据
//     */
//    @Test
//    public void testSyncLeaveRecords() {
//        LOG.info("开始同步 {} ~ {} 的请假信息", SYNC_START_DATE, SYNC_END_DATE);
//
//        int count = attendanceLeaveService.syncLeaveRecords(SYNC_START_DATE, SYNC_END_DATE);
//        LOG.info("同步完成，处理记录数: {}", count);
//
//        // 查询该范围入库数据校验
//        LocalDateTime rangeStart = SYNC_START_DATE.atStartOfDay();
//        LocalDateTime rangeEnd = SYNC_END_DATE.plusDays(1).atStartOfDay();
//        List<AttendanceLeave> leaves = attendanceLeaveMapper.selectList(
//                Wrappers.<AttendanceLeave>lambdaQuery()
//                        .lt(AttendanceLeave::getLeaveStartTime, rangeEnd)
//                        .gt(AttendanceLeave::getLeaveEndTime, rangeStart)
//                        .orderByAsc(AttendanceLeave::getLeaveStartTime)
//                        .orderByAsc(AttendanceLeave::getDingUserId));
//        LOG.info("{} ~ {} 范围有交集的请假记录共 {} 条", SYNC_START_DATE, SYNC_END_DATE, leaves.size());
//        for (AttendanceLeave leave : leaves) {
//            LOG.info("dingUserId={}, leaveCode={}, startTime={}, endTime={}, durationUnit={}, durationPercent={}",
//                    leave.getDingUserId(), leave.getLeaveCode(), leave.getLeaveStartTime(),
//                    leave.getLeaveEndTime(), leave.getDurationUnit(), leave.getDurationPercent());
//        }
//    }
//
//    /**
//     * 验证每日工时汇总分页查询的请假标记（leave: true/false）
//     */
//    @Test
//    public void testQueryDailySummaryWithLeave() {
//        PageResult<AttendanceDailySummaryDTO> result = attendanceRecordService.queryDailySummary(
//                SUMMARY_START_DATE, SUMMARY_END_DATE, 1, 50);
//        LOG.info("每日工时汇总: {} ~ {}, 共 {} 条, 当前页 {} 条",
//                SUMMARY_START_DATE, SUMMARY_END_DATE, result.getTotal(), result.getList().size());
//        for (AttendanceDailySummaryDTO dto : result.getList()) {
//            LOG.info("workDay={}, username={}, dingUserId={}, onDutyTime={}, offDutyTime={}, "
//                            + "onDutyResult={}, offDutyResult={}, durationHours={}, overtime={}, leave={}",
//                    dto.getWorkDay(), dto.getUsername(), dto.getDingUserId(), dto.getOnDutyTime(),
//                    dto.getOffDutyTime(), dto.getOnDutyTimeResult(), dto.getOffDutyTimeResult(),
//                    dto.getDurationHours(), dto.getOvertime(), dto.getLeave());
//        }
//        // 每条记录的leave字段都应被填充（true或false，不为null）
//        for (AttendanceDailySummaryDTO dto : result.getList()) {
//            assert dto.getLeave() != null : "leave字段不应为null: " + dto.getUsername() + " " + dto.getWorkDay();
//        }
//    }
//}
